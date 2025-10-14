package com.example.resume.resume.service;

import com.example.resume.resume.dto.*;
import com.example.resume.resume.entity.CandidateInfo;
import com.example.resume.resume.entity.HighlightSpan;
import com.example.resume.resume.entity.Resume;
import com.example.resume.resume.repository.CandidateInfoRepository;
import com.example.resume.resume.repository.HighlightSpanRepository;
import com.example.resume.resume.repository.ResumeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final CandidateInfoRepository candidateInfoRepository;
    private final HighlightSpanRepository highlightSpanRepository;
    private final PdfService pdfService;
    private final StorageService storageService;

    public ResumeService(ResumeRepository resumeRepository,
                         CandidateInfoRepository candidateInfoRepository,
                         HighlightSpanRepository highlightSpanRepository,
                         PdfService pdfService,
                         StorageService storageService) {
        this.resumeRepository = resumeRepository;
        this.candidateInfoRepository = candidateInfoRepository;
        this.highlightSpanRepository = highlightSpanRepository;
        this.pdfService = pdfService;
        this.storageService = storageService;
    }

    public ResumeUploadResponse upload(MultipartFile file, ResumeUploadRequest request) {
        // 1) 파일 저장
        String storageKey = storageService.store(file);

        // 2) 텍스트 추출
        String text = pdfService.extractText(toInputStream(file));

        // 3) Resume 저장
        Resume resume = Resume.builder()
                .displayName(request.getDisplayName())
                .originalFileName(file.getOriginalFilename())
                .storageKey(storageKey)
                .extractedText(text == null ? "" : text)
                .uploadedAt(LocalDateTime.now())
                .build();
        resumeRepository.save(resume);

        // 4) 후보자 기본정보가 있으면 저장
        if (hasCandidateInfo(request)) {
            CandidateInfo ci = CandidateInfo.builder()
                    .name(request.getCandidateName())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .school(request.getSchool())
                    .resume(resume)
                    .build();
            candidateInfoRepository.save(ci);
            resume.setCandidateInfo(ci);
        }

        return new ResumeUploadResponse(resume.getId(), file.getOriginalFilename());
    }

    @Transactional(readOnly = true)
    public Page<ResumeListItemDTO> list(Pageable pageable) {
        return resumeRepository.findAll(pageable)
                .map(r -> new ResumeListItemDTO(
                        r.getId(),
                        r.getDisplayName(),
                        r.getTotalScore(),
                        r.getGrade()
                ));
    }

    @Transactional(readOnly = true)
    public ResumeDetailDTO detail(Long id) {
        Resume r = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + id));

        List<HighlightSpan> spans = highlightSpanRepository.findByResume(r);

        CandidateInfoDTO candidate = (r.getCandidateInfo() == null)
                ? null
                : new CandidateInfoDTO(
                r.getCandidateInfo().getName(),
                r.getCandidateInfo().getPhone(),
                r.getCandidateInfo().getAddress(),
                r.getCandidateInfo().getSchool()
        );

        List<HighlightSpanDTO> highlightDTOs = spans.stream()
                .map(s -> new HighlightSpanDTO(s.getSection(), s.getStartOffset(), s.getEndOffset(), s.getTag()))
                .toList();

        return new ResumeDetailDTO(
                r.getId(),
                r.getDisplayName(),
                r.getOriginalFileName(),
                r.getStorageKey(),
                r.getExtractedText(),
                candidate,
                highlightDTOs,
                r.getTotalScore(),
                r.getGrade(),
                r.getUploadedAt()
        );
    }

    // ---- helpers ----

    private java.io.InputStream toInputStream(MultipartFile file) {
        try { return file.getInputStream(); }
        catch (Exception e) { throw new IllegalStateException("파일 스트림 생성 실패", e); }
    }

    private boolean hasCandidateInfo(ResumeUploadRequest req) {
        return (req.getCandidateName() != null && !req.getCandidateName().isBlank())
                || (req.getPhone() != null && !req.getPhone().isBlank())
                || (req.getAddress() != null && !req.getAddress().isBlank())
                || (req.getSchool() != null && !req.getSchool().isBlank());
    }
}
/*
역할: 업로드/목록/상세 “전체 흐름”을 담당.

upload(file, request): (1) 파일 저장(StorageService) → (2) 텍스트 추출(PdfService) → (3) Resume 생성/저장 → (4) CandidateInfo 저장(옵션) → (5) 업로드 결과 반환.

list(pageable): Resume 페이지 조회 → ResumeListItemDTO로 매핑.

detail(id): Resume + HighlightSpan 조회 → ResumeDetailDTO 조립.
어디서: 컨트롤러가 모든 엔드포인트에서 이 서비스를 호출.*/