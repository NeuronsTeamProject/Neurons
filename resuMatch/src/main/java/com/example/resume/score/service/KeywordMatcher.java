package com.example.resume.score.service;

import com.example.resume.score.dto.KeywordHitDTO;
import com.example.resume.score.entity.Keyword;
import com.example.resume.score.entity.MatchType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class KeywordMatcher {

    public MatchResult score(String text, List<Keyword> rules, boolean caseInsensitive) {
        int compScore = 0;
        List<KeywordHitDTO> hits = new ArrayList<>();
        String base = caseInsensitive ? text.toLowerCase() : text;

        for (Keyword kr : rules) {
            if (!kr.isActive()) continue;
            boolean hit;
            if (kr.getMatchType() == MatchType.REGEX) {
                int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
                hit = Pattern.compile(kr.getKeyword(), flags).matcher(text).find();
            } else {
                String key = caseInsensitive ? kr.getKeyword().toLowerCase() : kr.getKeyword();
                hit = base.contains(key);
            }
            int s = hit ? kr.getWeight() : 0;
            compScore += s;
            hits.add(new KeywordHitDTO(kr.getKeyword(), hit, s, kr.getMatchType().name()));
        }
        return new MatchResult(compScore, hits);
    }

    public record MatchResult(int score, List<KeywordHitDTO> detail) {}
}
