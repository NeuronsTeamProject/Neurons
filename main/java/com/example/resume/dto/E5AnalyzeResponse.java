package com.example.resume.dto;

import lombok.Data;

import java.util.List;

@Data
public class E5AnalyzeResponse {
    private Integer score;           // E5 측 점수
    private String keyword;          // "Java,Spring,..." 등
}
