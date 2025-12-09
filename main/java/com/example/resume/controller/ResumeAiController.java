package com.example.resume.controller;

import com.example.resume.dto.ResumeResponseDTO;
import com.example.resume.service.ResumeAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")  // 프론트 개발 서버 허용
public class ResumeAiController {

    private final ResumeAiService service;

    /**
     * 이력서 분석 요청
     *
     *  - URL  : POST /api/analyze
     *  - Body : multipart/form-data
     *      - file     : PDF 파일
     *      - job_role : "frontend" / "backend" / "uiux" 등
     */
    @PostMapping(
            value = "/api/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResumeResponseDTO analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("job_role") String jobRole
    ) {
        return service.analyze(file, jobRole);
    }

    /**
     * (선택) 저장된 결과 단건 조회
     *  - URL: GET /api/resumes/{id}
     */
    @GetMapping("/api/resumes/{id}")
    public ResumeResponseDTO getOne(@PathVariable Integer id) {
        return service.getOne(id);
    }
}
