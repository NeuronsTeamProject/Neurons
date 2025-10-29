package com.example.resume.resume.service;

import com.example.resume.resume.dto.*;
import com.example.resume.resume.entity.*;
import com.example.resume.resume.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final CandidateInfoRepository candidateInfoRepository;
    private final MatchedKeywordRepository matchedKeywordRepository;
    private final PdfService pdfService;
    private final StorageService storageService;

    public ResumeService(ResumeRepository resumeRepository,
                         CandidateInfoRepository candidateInfoRepository,
                         MatchedKeywordRepository matchedKeywordRepository,
                         PdfService pdfService,
                         StorageService storageService) {
        this.resumeRepository = resumeRepository;
        this.candidateInfoRepository = candidateInfoRepository;
        this.matchedKeywordRepository = matchedKeywordRepository;
        this.pdfService = pdfService;
        this.storageService = storageService;
    }

    public ResumeUploadResponse upload(MultipartFile file, ResumeUploadRequest request) {
        String storageKey = storageService.store(file);
        String text = pdfService.extractText(getStream(file));

        Resume resume = Resume.builder()
                .displayName(request.getDisplayName())
                .originalFileName(file.getOriginalFilename())
                .storageKey(storageKey)
                .extractedText(text == null ? "" : text)
                .uploadedAt(LocalDateTime.now())
                .build();
        resumeRepository.save(resume);

        if (hasCandidateInfo(request)) {
            CandidateInfo ci = CandidateInfo.builder()
                    .name(request.getCandidateName())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .school(request.getSchool())
                    .jobRole(request.getJobRole()) // ◀ 저장
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
                        r.getCandidateInfo() != null ? r.getCandidateInfo().getJobRole() : null,
                        r.getTotalScore(),
                        r.getGrade()
                ));
    }

    @Transactional(readOnly = true)
    public ResumeDetailDTO detail(Long id) {
        Resume r = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + id));

        CandidateInfoDTO candidate = (r.getCandidateInfo() == null) ? null :
                new CandidateInfoDTO(
                        r.getCandidateInfo().getName(),
                        r.getCandidateInfo().getPhone(),
                        r.getCandidateInfo().getAddress(),
                        r.getCandidateInfo().getSchool(),
                        r.getCandidateInfo().getJobRole()
                );

        // 키워드 카테고리별 그룹핑
        Map<String, List<String>> keywords = matchedKeywordRepository.findByResumeId(id)
                .stream()
                .collect(Collectors.groupingBy(
                        MatchedKeyword::getCategory,
                        Collectors.mapping(MatchedKeyword::getTag, Collectors.toList())
                ));

        return new ResumeDetailDTO(
                r.getId(),
                r.getDisplayName(),
                r.getOriginalFileName(),
                r.getStorageKey(),
                r.getExtractedText(),
                candidate,
                keywords,
                r.getTotalScore(),
                r.getGrade(),
                r.getSummaryComment(), // ◀ 총평
                r.getUploadedAt()
        );
    }

    // helpers
    private java.io.InputStream getStream(MultipartFile f) {
        try { return f.getInputStream(); }
        catch (Exception e) { throw new IllegalStateException("파일 스트림 생성 실패", e); }
    }
    private boolean hasCandidateInfo(ResumeUploadRequest req) {
        return notBlank(req.getCandidateName()) ||
                notBlank(req.getPhone()) ||
                notBlank(req.getAddress()) ||
                notBlank(req.getSchool()) ||
                notBlank(req.getJobRole());
    }
    private boolean notBlank(String s){ return s != null && !s.isBlank(); }
}
