package com.example.resume.controller;

import com.example.resume.dto.ResumeResponseDTO;
import com.example.resume.entity.CharInfo;
import com.example.resume.repository.CharInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequestMapping("/api")
public class ApplicantController {

    private final CharInfoRepository repository;

    // PDF 저장 폴더 (Service와 동일하게 맞추기)
    @Value("${app.upload.dir:C:/resume_uploads}")
    private String uploadDir;

    /**
     * 전체 지원자(분석 결과) 목록
     * - URL: GET /api/applicants
     */
    @GetMapping("/applicants")
    public List<ResumeResponseDTO> getApplicants() {
        List<CharInfo> list = repository.findAll();

        // 최근 게 위로 오게 (id 기준 내림차순)
        list.sort(Comparator.comparing(CharInfo::getId).reversed());

        return list.stream()
                .map(this::toResumeDto)
                .collect(Collectors.toList());
    }

    /**
     * 단건 조회
     * - URL: GET /api/applicants/{id}
     */
    @GetMapping("/applicants/{id}")
    public ResumeResponseDTO getApplicant(@PathVariable Integer id) {
        CharInfo entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 id: " + id));

        return toResumeDto(entity);
    }

    /**
     * PDF 다운로드/보기
     * - URL: GET /api/applicants/{id}/pdf
     */
    @GetMapping("/applicants/{id}/pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Integer id) {
        CharInfo entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 id: " + id));

        // 현재 ResumeAiService에서 pdf 컬럼에 savedPath.toString() 저장 중
        String savedPathString = entity.getPdf();
        if (savedPathString == null || savedPathString.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(savedPathString).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String filename = entity.getPdfName() != null ? entity.getPdfName() : "resume.pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename.replace("\"", "") + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResumeResponseDTO toResumeDto(CharInfo e) {
        String pdfUrl = "/api/applicants/" + e.getId() + "/pdf";

        return ResumeResponseDTO.builder()
                .id(e.getId())
                .pdfName(e.getPdfName())
                .role(e.getRole())          // ✅ 여기만 수정 (null → DB값)
                .score(e.getScore())
                .keyword(e.getKeyword())
                .aiSummary(e.getAiSummary())
                .pdfUrl(pdfUrl)
                .build();
    }
}
