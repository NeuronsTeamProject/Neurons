package com.example.resume.score.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScoreRequestDTO {
    @NotBlank
    private String resumeText;
    private ScoreOptionsDTO options = new ScoreOptionsDTO();
}
