package com.example.resume.dto;

import lombok.Data;

import java.util.List;

@Data
public class E5AnalyzeResponse {
    private Integer score;           // E5 측 점수
    private List<String> keywords;   // ["Java","Spring",...]
    private String summary;          // 선택: E5가 요약/총평도 줄 수 있으면

    // 혹시 E5가 keywords를 문자열로 보낼 때 대비(옵셔널):
    private String keyword;          // "Java,Spring,..." 등
}
