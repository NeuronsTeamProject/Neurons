package com.example.resume.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResumeUploadResponse {
    private Long resumeId;
    private String fileName;
}
/*
역할: 업로드 결과로 클라이언트에 돌려줄 최소 정보(resumeId, fileName).
어디서: ResumeService.upload()의 반환값으로 사용, 컨트롤러가 그대로 응답.
*/