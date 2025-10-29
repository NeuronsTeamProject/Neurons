package com.example.resume.score.service;

import com.example.resume.score.entity.Competency;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmbeddingScorer {
    public int score(String text, List<Competency> comps, int topk) {
        // 데모: 텍스트 길이에 따라 보너스(최대 5점)
        int bonus = Math.min(5, Math.max(0, text.length() / 1000));
        return bonus;
    }
}
