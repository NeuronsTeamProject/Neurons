package com.example.resume.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ResumeResponseDTO {
    private Integer id;
    private String pdfName;
    private Integer score;        // null 가능 (GPT 총평만 한 경우)
    private List<String> keywords;
    private String aiSummary;
}
