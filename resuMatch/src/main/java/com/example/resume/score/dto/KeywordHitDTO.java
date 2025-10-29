package com.example.resume.score.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeywordHitDTO {
    private String keyword;
    private boolean hit;
    private int score;
    private String matchType;
}
