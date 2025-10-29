package com.example.resume.score.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompetencyScoreDTO {
    private String name;
    private int score;
    private int max;
    private List<KeywordHitDTO> keywords;
}
