package com.example.resume.resume.controller;

import com.example.resume.resume.dto.*;
import com.example.resume.resume.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    // 업로드: 멀티파트 파일 + 선택 입력 필드
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeUploadResponse upload(
            @RequestPart("file") MultipartFile file,
            @Valid @ModelAttribute ResumeUploadRequest request
    ) {
        return resumeService.upload(file, request);
    }

    // 목록 조회 (좌측 패널용): 페이지네이션
    @GetMapping
    public Page<ResumeListItemDTO> list(Pageable pageable) {
        return resumeService.list(pageable);
    }

    // 상세 조회 (대시보드 이동용)
    @GetMapping("/{id}")
    public ResumeDetailDTO detail(@PathVariable Long id) {
        return resumeService.detail(id);
    }
}
/*
역할: HTTP API 엔드포인트 제공.

POST /api/resumes (multipart): 파일 + 폼(표시이름/인적사항) 수신 → resumeService.upload() 호출 → 업로드 결과 반환.

GET /api/resumes (목록): 페이지 요청 → resumeService.list() 응답 DTO 반환.

GET /api/resumes/{id} (상세): resumeService.detail(id) 결과 반환.
어디서: 프론트엔드(좌측 목록, 상세 대시보드, 업로드 화면)와 직접 맞닿는 진입점.*/