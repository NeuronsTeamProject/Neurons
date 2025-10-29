package com.example.resume.score.dto;

import lombok.Data;

@Data
public class ScoreOptionsDTO {
    private boolean useEmbedding = false;
    private Integer topk = 5;
    private boolean caseInsensitive = true;
}
