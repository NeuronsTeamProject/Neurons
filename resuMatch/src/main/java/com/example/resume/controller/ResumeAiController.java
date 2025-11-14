package com.example.resume.controller;

import com.example.resume.dto.ResumeResponseDTO;
import com.example.resume.service.ResumeAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resumes")
public class ResumeAiController {

    private final ResumeAiService service;

    // (1) 프론트→백엔드(파일) → E5 파이썬 서비스 프록시 → 점수/키워드 수신 & 저장
    @PostMapping(value = "/e5/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeResponseDTO analyzeWithE5(@RequestPart("file") MultipartFile file) {
        return service.analyzeWithE5(file);
    }

    // (2) 프론트→백엔드(파일) → GPT API에 파일 그대로 전달, 총평 수신 & 저장
    @PostMapping(value = "/gpt/summary", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeResponseDTO summarizeWithGpt(@RequestPart("file") MultipartFile file) {
        return service.summarizeWithGpt(file);
    }

    // (3) 단건 조회 (프론트에서 결과 재표시)
    @GetMapping("/{id}")
    public ResumeResponseDTO getOne(@PathVariable Integer id) {
        return service.getOne(id);
    }

}
