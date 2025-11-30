'''
- analyze.py의 로직을 감싸서, API 요청으로부터 데이터를 받고
  로직을 실행한 뒤, 결과를 schemas.py에 정의된 형식에 맞게 가공하여 반환하는 역할

- API서버나 다른 백엔드 레이어에서 e5_analyze_api만 호출해서 나온 dict를 그대로 JSON으로 응답하면 됩니다.
'''
from analyze import analyze_resume

def e5_analyze_api(pdf_path: str, job_role: str) -> dict:
    base_result = analyze_resume(pdf_path, job_role)

    # 점수 : int
    total_score = int(round(base_result["total_sum"]))

    # 매칭된 키워드 : 문자열로 변환(중복제거 및 정렬 수행)
    unique_keywords = sorted(set(match["keyword"] for match in base_result["matches"]))
    keywords = ", ".join(unique_keywords)
    
    # 백엔드로 전달할 score, keyword -> 명칭 정확하게 전달
    return {
        "score": total_score,
        "keyword": keywords
    }