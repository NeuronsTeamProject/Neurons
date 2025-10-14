package com.example.resume.resume.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    /**
     * 파일을 저장하고 고유 storageKey(경로/파일명)를 반환한다.
     */
    String store(MultipartFile file);

    /**
     * 저장된 파일을 다운로드할 프리사인 URL 또는 내부 키를 반환하도록 확장 가능.
     */
    default String getPublicUrl(String storageKey) { return null; }
}
/*
역할: 파일 저장 추상화(S3/MinIO/로컬 등 구현체 교체). store()가 저장키(storageKey)를 반환.
어디서: ResumeService.upload()에서 업로드 파일을 저장하고 경로/키를 Resume.storageKey에 기록.
*/