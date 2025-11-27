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
@CrossOrigin(origins = "http://localhost:5173")  // 프론트 개발 서버 허용
public class ResumeAiController {

    private final ResumeAiService service;

    /**
     * (메인) 이력서 분석 엔드포인트
     *
     * 프론트에서 PDF + jobRole(+ name)을 보내면:
     *  1) E5 분석 (score, keyword)
     *  2) GPT 요약 (aiSummary)
     *  3) DB에 한 줄로 저장
     *  4) 저장된 결과를 JSON으로 프론트에 바로 응답
     *
     * 최종 엔드포인트: POST /api/resumes/result
     *
     * 프론트 예시 (React):
     *
     * const formData = new FormData();
     * formData.append("file", selectedFile);
     * formData.append("jobRole", selectedRole);       // 예: "프론트엔드"
     * formData.append("name", resumeName);            // 예: "홍길동_프론트엔드_이력서"
     *
     * const res = await fetch("http://localhost:8080/api/resumes/result", {
     *   method: "POST",
     *   body: formData,
     * });
     * const data = await res.json();
     */
    @PostMapping(
            value = "/result",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResumeResponseDTO analyzeAll(
            @RequestPart("file") MultipartFile file,
            @RequestParam("jobRole") String jobRole,
            @RequestParam(value = "name", required = false) String name
    ) {
        return service.analyzeAll(file, jobRole, name);
    }

    /**
     * 저장된 결과 단건 조회
     * GET /api/resumes/{id}
     */
    @GetMapping("/{id}")
    public ResumeResponseDTO getOne(@PathVariable Integer id) {
        return service.getOne(id);
    }
}
