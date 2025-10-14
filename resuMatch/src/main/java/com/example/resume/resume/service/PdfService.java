package com.example.resume.resume.service;

import java.io.InputStream;

public interface PdfService {
    String extractText(InputStream in);
}
/*
역할: PDF → 텍스트 추출 계약(구현체 교체 가능: PDFBox/추후 서버 호출 등).
어디서: ResumeService.upload()에서 파일 입력스트림을 넘겨 추출 텍스트를 받음.
*/