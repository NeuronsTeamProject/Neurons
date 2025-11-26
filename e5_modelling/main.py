# 모델 성능 테스트를 위한 로컬 실행환경 코드파일

from analyze import analyze_resume

if __name__ == "__main__":
    pdf_path = "e5_modelling/resume.pdf"

    # backend / frontend / uiux 중 하나의 값을 선택하여 테스트 진행
    job_role = "backend"

    result = analyze_resume(pdf_path, job_role)


# 분석결과 출력

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
