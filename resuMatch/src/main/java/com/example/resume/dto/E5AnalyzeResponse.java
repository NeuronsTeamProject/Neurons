package com.example.resume.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class E5AnalyzeResponse {

    // 파이썬 JSON: "total_sum": 32.0
    @JsonProperty("total_sum")
    private Double totalSum;

    // 파이썬 JSON: "matches": [...]
    private List<Match> matches;

    /**
     * 기존 코드 호환용:
     * Service에서 e5.getScore()를 부르면 total_sum 기반으로 점수 반환
     */
    @JsonIgnore
    public Integer getScore() {
        if (totalSum == null) return null;
        return (int) Math.round(totalSum);
    }

    /**
     * 기존 코드 호환용:
     * Service에서 e5.getKeyword()를 부르면 matches에서 keyword만 뽑아서 CSV로 반환
     */
    @JsonIgnore
    public String getKeyword() {
        if (matches == null || matches.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Match m : matches) {
            if (m == null) continue;
            String k = m.getKeyword();
            if (k == null || k.isBlank()) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(k.trim());
        }
        return sb.toString();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Match {
        private String section;
        private String sentence;
        private String keyword;
        private Double sim;
        private Double score;

        @JsonProperty("used_template")
        private String usedTemplate;
    }
}
