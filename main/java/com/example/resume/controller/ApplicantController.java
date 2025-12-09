package com.example.resume.controller;

import com.example.resume.dto.ApplicantResponseDTO;
import com.example.resume.entity.CharInfo;
import com.example.resume.repository.CharInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api")
public class ApplicantController {

    private final CharInfoRepository repository;

    // 필요하다면 기본 지역/경력 설정값을 application.properties 에서 뺄 수도 있음
    @Value("${app.default.location:부산}")
    private String defaultLocation;

    @Value("${app.default.career:신입}")
    private String defaultCareer;

    /**
     * 지원자 목록 조회
     *  - GET /api/applicants
     *  - 반환: List<ApplicantResponseDTO>
     */
    @GetMapping("/applicants")
    public List<ApplicantResponseDTO> getApplicants() {
        List<CharInfo> entities = repository.findAll();

        // id DESC 정렬 (최근 추가 순)
        entities.sort(Comparator.comparing(CharInfo::getId).reversed());

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ───────────────── private mapping helpers ─────────────────

    private ApplicantResponseDTO toDto(CharInfo entity) {

        // 1) 이름: pdf_name에서 확장자(.pdf) 제거
        String pdfName = Optional.ofNullable(entity.getPdfName()).orElse("이력서");
        String name = stripExtension(pdfName);

        // 2) 카테고리: job_role(영문/한글 둘 다 가능)을 한글로 변환
        String category = toKoreanCategory(entity.getRole());

        // 3) 점수
        Integer score = entity.getScore() != null ? entity.getScore() : 0;

        // 4) 키워드 → skills.required 로 넣기 (우선 전부 필수 기술로)
        String keywordString = Optional.ofNullable(entity.getKeyword()).orElse("");
        List<String> allKeywords = Arrays.stream(keywordString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        ApplicantResponseDTO.SkillsDTO skills = ApplicantResponseDTO.SkillsDTO.builder()
                .required(allKeywords)
                .preferred(Collections.emptyList())  // 나중에 분리하고 싶으면 여기 로직 추가
                .tools(Collections.emptyList())
                .build();

        // 5) 분석/강점: ai_summary 재활용
        String summary = entity.getAiSummary();
        String analysis = summary != null ? summary : "AI 분석 결과가 아직 없습니다.";
        String strengths = summary != null ? summary : "강점 분석 결과가 아직 없습니다.";

        return ApplicantResponseDTO.builder()
                .id(entity.getId())
                .name(name)
                .category(category)
                .score(score)
                .location(defaultLocation)
                .career(defaultCareer)
                .skills(skills)
                .analysis(analysis)
                .strengths(strengths)
                .build();
    }

    private String stripExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx > 0) {
            return fileName.substring(0, idx);
        }
        return fileName;
    }

    /**
     * DB에 저장된 job_role 값을 한글 카테고리로 매핑
     *  - frontend / 프론트엔드 → 프론트엔드
     *  - backend / 백엔드 → 백엔드
     *  - uiux / 기획자 / UI/UX → 기획자
     *  - 그 외: 원래 문자열 그대로
     */
    private String toKoreanCategory(String jobRole) {
        if (jobRole == null) return "기타";

        String v = jobRole.trim().toLowerCase();

        switch (v) {
            case "frontend":
            case "프론트엔드":
            case "front-end":
                return "프론트엔드";
            case "backend":
            case "백엔드":
            case "back-end":
                return "백엔드";
            case "uiux":
            case "ui/ux":
            case "기획자":
                return "기획자";
            default:
                return jobRole; // 모르는 값이면 있는 그대로 노출
        }
    }
}
