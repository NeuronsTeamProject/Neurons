'''
- 프론트로부터 업로드 된 pdf파일을 백엔드로부터 받아와서 분석을 하기위한 코드입니다.
- PDF파일을 파일명이 포함된 상태가 아닌 바이너리 형태로 받아오기 때문에
  임시 경로(파일)를 생성해서 내용을 저장하고 분석 후 제거하는 방식으로 진행합니다.
'''

from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from services import e5_analyze_api
import tempfile
import os
import logging

logger = logging.getLogger(__name__)

app = FastAPI(
    title="Resume Analyze E5 API",
    description="E5를 활용한 이력서 선별 서비스",
    version="1.0.0",
)

# 필요 시 수정해서 사용(CORS(Cross-Origin Resource Sharing) 허용 규칙 설정)
# 브라우저는 도메인/포트가 다르면 보안 때문에 기본적으로 요청을 막기 때문에 명시적 허용
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],
    allow_credentials=True,     # 쿠키, 인증 토큰 등의 인증정보 전달 허용
    allow_methods=["*"],        # 모든 HTTP 메서드 허용
    allow_headers=["*"],        # 모든 헤더 허용
)

# FastAPI 서버가 정상적으로 실행 중인지 확인하기 위한 API
@app.get("/health")
async def health_check():
    return {"status": "ok"}

# 출력되는 형식 지정
@app.post("/analyze")
async def analyze_resume_endpoint(
    file: UploadFile = File(...),
    job_role: str = Form(...),
):

    # 파일 타입 검증
    content_type = (file.content_type or "").lower()
    if not (
        content_type == "application/pdf"
        or content_type.startswith("application/octet-stream")
    ):
        raise HTTPException(status_code=400, detail="PDF 파일만 업로드 가능합니다.")

    # 업로드된 파일을 임시 경로에 저장(파일명을 받아오지 않기 때문!)
    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=".pdf") as tmp:
            temp_path = tmp.name

            # chunk 단위로 읽어서 저장 (대용량 파일 대비)
            while True:
                chunk = await file.read(1024 * 1024)  # 1MB씩 읽도록 설정
                if not chunk:
                    break
                tmp.write(chunk)

    except Exception as e:
        logger.exception("파일 저장 중 오류 발생")
        raise HTTPException(status_code=500, detail=f"파일 저장 중 오류가 발생했습니다: {e}")
    
    finally:
        # UploadFile 스트림 닫기
        await file.close()

    # 3) E5 분석 호출
    try:
        # services.e5_analyze_api는 (pdf_path, job_role)을 받도록 구현되어 있음
        result = e5_analyze_api(temp_path, job_role)
        return result

    # 오류처리
    except Exception as e:
        logger.exception("이력서 분석 중 오류 발생")
        raise HTTPException(status_code=500, detail=f"이력서 분석 중 오류가 발생했습니다: {e}")

    finally:
        # 4) 임시 파일 삭제
        try:
            if os.path.exists(temp_path):
                os.remove(temp_path)

        except Exception:
            # 삭제 실패는 서비스 로직과 직접 연관 없는 부분이므로 로그만 남김
            logger.warning("임시 파일 삭제 실패: %s", temp_path)