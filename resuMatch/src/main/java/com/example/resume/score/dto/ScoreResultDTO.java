package com.example.resume.score.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ScoreResultDTO {
    private int totalScore;
    private String grade;
    private String summaryComment;
    private List<CompetencyScoreDTO> competencies;
}
