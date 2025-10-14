package com.example.resume.resume.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResumeUploadRequest {
    // 선택 입력 (있으면 후보자 기본정보로 저장)
    private String candidateName;
    private String phone;
    private String address;
    private String school;

    // UI에서 파일명과 별개로 표시하고 싶을 때
    @NotBlank(message = "표시 이름은 비울 수 없습니다.")
    private String displayName;
}
/*
역할: 업로드 폼 데이터(표시이름, 선택 인적사항)를 받는 요청 DTO.
어디서: ResumeController.upload()에서 @ModelAttribute로 바인딩되어 ResumeService.upload()에 전달.
*/