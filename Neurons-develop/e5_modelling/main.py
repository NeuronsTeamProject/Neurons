# e5_modelling/main.py
"""
E5 모델 로컬 테스트 & 스프링 연동용 공통 엔트리 포인트.

1) 스프링에서 호출할 때
   python main.py <pdf_path> <job_role>
   예) python main.py C:/Temp/resume-1234.pdf backend

2) 터미널에서 직접 테스트할 때
   그냥 python main.py 만 실행하면
   기본값 (e5_modelling/resume.pdf, backend) 로 테스트한다.
"""

import sys
import json
from analyze import analyze_resume


def main():
    # ----------------------------------------
    # 1. 인자 파싱
    # ----------------------------------------
    if len(sys.argv) >= 3:
        # 스프링에서 호출하는 형태: python main.py <pdf_path> <job_role>
        pdf_path = sys.argv[1]
        job_role = sys.argv[2]
    else:
        # 로컬 테스트용 기본값
        pdf_path = "e5_modelling/resume.pdf"
        job_role = "backend"
        print("[main.py] 인자가 없어 기본 테스트 모드로 실행합니다.")
        print(f"[main.py] pdf_path = {pdf_path}")
        print(f"[main.py] job_role = {job_role}")

    # ----------------------------------------
    # 2. 실제 분석 호출
    #    analyze_resume 함수는 기존 코드 그대로 사용
    # ----------------------------------------
    result = analyze_resume(pdf_path, job_role)

    # ----------------------------------------
    # 3. 출력 형식
    #    - 스프링(E5Client)은 stdout 을 읽어서 JSON 으로 파싱한다고 가정
    #    - 로컬에서 보기에도 편하게 JSON 문자열로 출력
    # ----------------------------------------
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()
