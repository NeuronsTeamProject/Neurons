'''
섹션 분류 + 의미 스코어링 + 자격어학 스코어링 로직 관련 파일입니다.
주요 비즈니스 로직을 구현합니다.(문장 분류, 키워드 매칭, 점수 부여, 오탐 방지로직)
'''
import re
import torch
from collections import defaultdict
from config import (
    technical_keywords,
    cert_keywords,
    section_labels,
    KEYWORD_TEMPLATES,
    KEYWORD_GATES,
    THRESHOLD_SECTION,
    THRESHOLD_KEYWORD,
)
from embedding import embed_text, e5_query, e5_passage



# 쓰레기 문장 필터함수
def is_informative_sentence(sentence: str) -> bool:
    s = sentence.strip()
    # 매칭된 문장이 너무 짧으면 버리기(6글자)
    if len(s) < 6:
        return False
    # 숫자/기호만 있는 경우 버리기
    if re.fullmatch(r"^[\d\-\.\)\(※\*]+$", s):
        return False
    # 전형적인 양식/서약/관리 문구 버리기
    boilerplate_patterns = [
        "사실에 근거하여 성실히 작성하였음을 서약합니다",
        "사실과 다름없음을 확인합니다",
        "위 내용은 사실과 다름없습니다",
        "본인은 이에 동의합니다",
        "개인정보의 수집 및 이용에 동의합니다",
        "정보 제공에 동의합니다."
    ]
    for pat in boilerplate_patterns:
        if pat in s:
            return False
    
    return True


# 자격/어학 성격의 문장인지 강하게 체크
def rule_based_precheck(sentence: str):

    # 자격/어학에 등장할 법한 단어가 하나라도 있으면 무조건 '자격어학'으로 분류
    if any(word in sentence for word in [
        "정보처리기사", "빅데이터분석기사", "정보보안기사",
        "SQLD", "TOEIC", "토익", "OPIC", "오픽", "JLPT", "TOPIK",
        "자격증", "어학", "점수"
    ]):
        return "자격어학"
    return None


# 문장을 섹션으로 자동 분류
def classify_sentences_by_section(sentences):

    section_names = list(section_labels.keys())
    section_descs = list(section_labels.values())

    # 1) 섹션 정의문은 "query:" 역할
    section_queries = [e5_query(desc) for desc in section_descs]
    section_emb = embed_text(section_queries)          # [num_sections, dim]

    # 2) 이력서 문장은 "passage:" 역할
    passage_texts = [e5_passage(s) for s in sentences]
    sentence_emb = embed_text(passage_texts)           # [num_sentences, dim]

    # 3) 유사도 계산 (문장 x 섹션)
    similarity = sentence_emb @ section_emb.T          # [num_sentences, num_sections]
    section_buckets = {name: [] for name in section_names}
    unassigned = []         # 임계값 미달 문장들을 보낼 버킷


    for i, row in enumerate(similarity):
        sent = sentences[i]
        forced_section = rule_based_precheck(sent)     # 기존 rule-based 자격어학 우선 분류 유지
        if forced_section:
            section_buckets[forced_section].append(sent)
            continue

        best_idx = torch.argmax(row).item()
        best_section = section_names[best_idx]
        best_score = float(row[best_idx])

        if best_score >= THRESHOLD_SECTION:
            section_buckets[best_section].append(sent)
        else:
            unassigned.append(sent)     # 임계값 미달은 따로 모으기

    section_buckets["_UNASSIGNED"] = unassigned
    return section_buckets



# A. 자격어학 섹션 점수 계산 (키워드 포함 여부만) - 쓰레기 문장 필터는 굳이 추가X
def score_section_cert(sentences, section_name, awarded_keywords_global):
    section_total_score = 0.0
    match_log = []

    for sent in sentences:
        for kw, kw_score in cert_keywords.items():
            if kw in sent:
                # 한 번 매칭된 자격증은 다시 매칭 제한
                if kw in awarded_keywords_global:
                    continue
                awarded_keywords_global.add(kw)

                section_total_score += kw_score
                match_log.append({
                    "section": section_name,
                    "sentence": sent,       # 키워드가 매칭된 근거문장 저장
                    "keyword": kw,
                    "sim": None,          # 의미 유사도 안 씀
                    "score": kw_score,    # 그대로 점수 부여
                })

    return section_total_score, match_log



# B. 일반 섹션 점수 계산 (의미 유사도 기반)
def score_section_semantic(sentences, section_name, awarded_keywords_global):
    # 1) 의미 없는 문장 제거
    filtered_sentences = [s for s in sentences if is_informative_sentence(s)]

    section_total_score = 0.0
    match_log = []

    # 2) 필터 후 문장이 하나도 없으면 그냥 0점 리턴
    if not filtered_sentences:
        return section_total_score, match_log

    # 3) 문장 임베딩
    passage_texts = [e5_passage(s) for s in filtered_sentences]
    sentence_emb = embed_text(passage_texts)  # shape: [num_sentences, dim]

    canonical_keywords, template_texts,template_owner = [], [], []      

    for _, kw_data in technical_keywords.items():
        for kw in kw_data.keys():
            # 해당 키워드에 등록된 템플릿이 있으면 사용, 없으면 키워드 자체를 문장화
            templates = KEYWORD_TEMPLATES.get(kw, [f"{kw}와 관련된 경험이 있다"])
            for t in templates:
                canonical_keywords.append(kw)
                template_texts.append(e5_query(t))
                template_owner.append(kw)

    template_emb = embed_text(template_texts)  # [num_templates, dim]

    # ③ 문장 x 템플릿 유사도
    #    이후 "정식 키워드" 단위로 최대값을 집계
    sim_mat = sentence_emb @ template_emb.T
    owner_to_indices = defaultdict(list)
    for idx, owner in enumerate(template_owner):
        owner_to_indices[owner].append(idx)

    # ⑤ 각 문장에 대해 "정식 키워드" 별 최대 유사도 계산 → best_kw 선택
    for sent_idx in range(sim_mat.shape[0]):
        best_kw = None
        best_sim = -1.0
        best_template_idx = None
        row = sim_mat[sent_idx]

        for kw, indices in owner_to_indices.items():
            # 이 키워드의 모든 템플릿에 대한 최대 유사도
            sims = row[indices]
            kw_max = float(torch.max(sims))
            if kw_max > best_sim:
                best_sim = kw_max
                best_kw = kw
                # 최대가 나온 템플릿 인덱스 찾기 (근거용)
                local_best = torch.argmax(sims).item()
                best_template_idx = indices[local_best]

        gate = KEYWORD_GATES.get(best_kw)

        # gate가 정의되어 있다면, 조건을 만족하는 경우에만 인정
        if gate is not None and not gate(filtered_sentences[sent_idx], section_name):
                if best_sim < (THRESHOLD_KEYWORD + 0.05):        # 유사도 높으면 통과
                    continue

        # ⑥ 임계값 통과 + 중복 방지 후 점수 부여
        if best_sim >= THRESHOLD_KEYWORD and best_kw not in awarded_keywords_global:
            # 점수표에서 스코어 가져오기 (첫 카테고리에서 찾음)
            kw_score = None
            for _, kw_data in  technical_keywords.items():
                if best_kw in kw_data:
                    kw_score = kw_data[best_kw]
                    break
            if kw_score is None:
                continue

            awarded_keywords_global.add(best_kw)
            section_total_score += kw_score
            match_log.append({
                "section": section_name,
                "sentence": filtered_sentences[sent_idx],
                "keyword": best_kw,
                "sim": round(best_sim, 3),
                "score": kw_score,
                "used_template": template_texts[best_template_idx],  # 디버깅/설명용
            })

    return section_total_score, match_log