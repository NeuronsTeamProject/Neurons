package com.example.resume.score.controller;

import com.example.resume.score.dto.ScoreRequestDTO;
import com.example.resume.score.dto.ScoreResultDTO;
import com.example.resume.score.service.ScoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoringService scoringService;

    @PostMapping(value = "/evaluate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ScoreResultDTO evaluate(@Valid @RequestBody ScoreRequestDTO request) {
        return scoringService.evaluate(request.getResumeText(), request.getOptions());
    }

    @GetMapping("/health")
    public String health() { return "OK"; }
}
