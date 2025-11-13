'''
프로젝트 전체의 워크플로우를 담당하는 진입점 입니다.
'PDF 읽고/문장 자르고/scoring 호출해서/결과 반환'로직으로 구성
'''
import pdfplumber
import nltk
from scoring import (
    classify_sentences_by_section,
    score_section_cert,
    score_section_semantic,
)

# 초기 설정
# nltk 리소스 다운로드 (최초 1회만 주석 해제해서 실행)
# nltk.download('punkt')
# nltk.download('punkt_tab')


# PDF 텍스트 추출
def extract_text_from_pdf(pdf_path: str) -> str:
    text = ""
    with pdfplumber.open(pdf_path) as pdf:
        for page in pdf.pages:
            content = page.extract_text()
            if content:
                text += content + "\n"
    return text.strip()

# 문장 분리
def split_sentences(text: str):
    return nltk.sent_tokenize(text)


def analyze_resume(pdf_path: str):
    # 1) PDF → 전체 텍스트 추출 → 문장 분리
    text = extract_text_from_pdf(pdf_path)
    sentences = split_sentences(text)

    # 2) 문장들을 의미적으로 섹션에 분류
    section_buckets = classify_sentences_by_section(sentences)
    total_sum = 0.0
    match_log = []

    awarded_keywords_global = set()     # 전역 키워드 중복 장지 세트

    # 3) 섹션별 점수 계산
    for section_name, section_sentences in section_buckets.items():
        if not section_sentences:
            continue

        if section_name == "자격어학":
            # [자격/어학] - 키워드 포함 여부로 판단
            section_score, matches = score_section_cert(
                section_sentences,
                section_name,
                awarded_keywords_global
            )
        else:
            # [자기소개/경험사례/협업경험] - 의미 유사도 기반으로 판단
            section_score, matches = score_section_semantic(
                section_sentences,
                section_name,
                awarded_keywords_global
            )

        total_sum += section_score
        match_log.extend(matches)

    # 4) 결과 리턴
    return {
        "total_sum": total_sum,
        "matches": match_log
    }


# 분석결과 출력

if __name__ == "__main__":
    pdf_file = "e5_modelling/resume.pdf"  # 분석하려는 PDF 파일명
    result = analyze_resume(pdf_file)

    print("\n[요약 결과]")
    print(f"총합 점수: {result['total_sum']}\n")

    print("[매칭된 키워드 요약]")
    for match in result["matches"]:
        section = match["section"]
        keyword = match["keyword"]
        sim = match["sim"]
        score = match["score"]
        sentence = match["sentence"]

        # 유사도가 None인 경우(자격어학 룰 기반)는 'N/A'로 처리
        sim_display = "N/A" if sim is None else sim

        print(f" - [{section}] '{keyword}' (유사도: {sim_display}, 점수부여: {score})")
        print(f"    ↳ 근거 문장: \"{sentence}\"")