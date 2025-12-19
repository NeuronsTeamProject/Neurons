package com.example.resume.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeResponseDTO {

    private Integer id;

    private String pdfName;

    private String role;   // = job_role

    private Integer score;

    private String keyword;

    private String aiSummary;

    private String pdfUrl; // 파생 필드 (프론트 편의용)
}