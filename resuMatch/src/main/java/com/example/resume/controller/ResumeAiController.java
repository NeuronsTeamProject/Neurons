package com.example.resume.controller;

import com.example.resume.dto.ResumeResponseDTO;
import com.example.resume.service.ResumeAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequestMapping("/api")
public class ResumeAiController {

    private final ResumeAiService service;

    /**
     * 이력서 분석 요청
     * - URL  : POST /api/analyze
     * - Body : multipart/form-data
     *   - file     : PDF 파일
     *   - job_role : "frontend" / "backend" / "uiux" 등
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeResponseDTO analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("job_role") String jobRole
    ) {
        // Service에서:
        // 1) 디스크 저장
        // 2) DB에는 pdf 컬럼에 경로 저장 (VARCHAR(500))
        // 3) 응답엔 pdfUrl 내려줌 (예: /api/applicants/{id}/pdf)
        return service.analyzeWithE5(file, jobRole);
    }

    /**
     * (선택) 단건 조회가 필요하면 Service에 getOne 같은 메서드 만들어서 붙여도 됨.
     * 지금은 Applicants 쪽에서 리스트/상세를 담당하도록 분리하는 편이 깔끔해서 비워둠.
     */
}
