package com.example.resume.score.service;

import com.example.resume.score.dto.CompetencyScoreDTO;
import com.example.resume.score.dto.ScoreOptionsDTO;
import com.example.resume.score.dto.ScoreResultDTO;
import com.example.resume.score.entity.Competency;
import com.example.resume.score.entity.GradeThreshold;
import com.example.resume.score.repository.CompetencyRepository;
import com.example.resume.score.repository.GradeThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private final CompetencyRepository competencyRepository;
    private final GradeThresholdRepository gradeThresholdRepository;
    private final KeywordMatcher keywordMatcher;
    private final EmbeddingScorer embeddingScorer;

    @Transactional(readOnly = true)
    public ScoreResultDTO evaluate(String text, ScoreOptionsDTO opts) {
        List<Competency> comps = competencyRepository.findByActiveTrueOrderByNameAsc();
        List<GradeThreshold> thresholds = gradeThresholdRepository.findAllByOrderByMinScoreDesc();

        int total = 0;
        var compResults = new java.util.ArrayList<CompetencyScoreDTO>();

        for (var comp : comps) {
            var match = keywordMatcher.score(text, comp.getKeywords(), opts.isCaseInsensitive());
            int weighted = match.score() * Math.max(1, comp.getWeight());
            total += weighted;

            int max = comp.getKeywords().stream()
                    .filter(k -> k.isActive())
                    .mapToInt(k -> k.getWeight() * Math.max(1, comp.getWeight()))
                    .sum();

            compResults.add(new CompetencyScoreDTO(comp.getName(), weighted, max, match.detail()));
        }

        if (opts.isUseEmbedding()) {
            total += embeddingScorer.score(text, comps, opts.getTopk() == null ? 5 : opts.getTopk());
        }

        String grade = mapGrade(total, thresholds);
        String comment = buildSummaryComment(total, grade, compResults);

        return new ScoreResultDTO(total, grade, comment, compResults);
    }

    private String mapGrade(int total, List<GradeThreshold> t) {
        if (t == null || t.isEmpty()) return "N/A";
        return t.stream()
                .sorted(Comparator.comparingInt(GradeThreshold::getMinScore).reversed())
                .filter(th -> total >= th.getMinScore())
                .map(GradeThreshold::getGrade)
                .findFirst().orElse("N/A");
    }

    private String buildSummaryComment(int total, String grade, List<CompetencyScoreDTO> comps) {
        var top = comps.stream()
                .sorted(Comparator.comparingInt(CompetencyScoreDTO::getScore).reversed())
                .limit(2).map(CompetencyScoreDTO::getName).toList();
        String strengths = top.isEmpty() ? "특이사항 없음" : String.join(", ", top);
        return String.format("총점 %d점, 등급 %s. 강점: %s.", total, grade, strengths);
    }
}
