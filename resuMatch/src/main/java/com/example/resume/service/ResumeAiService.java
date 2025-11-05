package com.example.resume.service;

import com.example.resume.dto.E5AnalyzeResponse;
import com.example.resume.dto.ResumeResponseDTO;
import com.example.resume.entity.CharInfo;
import com.example.resume.repository.CharInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeAiService {

    private final E5Client e5Client;
    private final GptClient gptClient;
    private final CharInfoRepository repository;

    // 1) E5 분석 프록시 + DB 저장
    @Transactional
    public ResumeResponseDTO analyzeWithE5(MultipartFile file) {
        try {
            E5AnalyzeResponse e5 = e5Client.analyzeResume(file);

            List<String> keywords = (e5.getKeywords() != null)
                    ? e5.getKeywords()
                    : parseKeywordString(e5.getKeyword());

            CharInfo saved = repository.save(CharInfo.builder()
                    .pdfName(safeName(file.getOriginalFilename()))
                    .pdf(file.getBytes()) // 프론트에서 받은 PDF 그대로 저장 (요구 스키마 준수)
                    .score(e5.getScore())
                    .keyword(keywords != null ? String.join(",", keywords) : null)
                    .aiSummary(e5.getSummary())
                    .build());

            return ResumeResponseDTO.builder()
                    .id(saved.getId())
                    .pdfName(saved.getPdfName())
                    .score(saved.getScore())
                    .keywords(keywords)
                    .aiSummary(saved.getAiSummary())
                    .build();

        } catch (Exception ex) {
            throw new RuntimeException("E5 분석 처리 실패: " + ex.getMessage(), ex);
        }
    }

    // 2) GPT 총평 + DB 저장 (텍스트 추출 없이 파일 자체 전달)
    @Transactional
    public ResumeResponseDTO summarizeWithGpt(MultipartFile file) {
        try {
            String summary = gptClient.summarizePdf(file.getBytes(), safeName(file.getOriginalFilename()));

            CharInfo saved = repository.save(CharInfo.builder()
                    .pdfName(safeName(file.getOriginalFilename()))
                    .pdf(file.getBytes())
                    .score(null) // 총평만 실행, 점수 없음
                    .keyword(null)
                    .aiSummary(trimToLength(summary, 300)) // DB 스키마 길이(300) 준수
                    .build());

            return ResumeResponseDTO.builder()
                    .id(saved.getId())
                    .pdfName(saved.getPdfName())
                    .score(null)
                    .keywords(Collections.emptyList())
                    .aiSummary(saved.getAiSummary())
                    .build();

        } catch (Exception ex) {
            throw new RuntimeException("GPT 총평 처리 실패: " + ex.getMessage(), ex);
        }
    }

    // 3) 단건 조회 (프론트 연동 편의)
    @Transactional(readOnly = true)
    public ResumeResponseDTO getOne(Integer id) {
        CharInfo ci = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        List<String> keywords = (ci.getKeyword() == null || ci.getKeyword().isBlank())
                ? Collections.emptyList()
                : Arrays.asList(ci.getKeyword().split("\\s*,\\s*"));

        return ResumeResponseDTO.builder()
                .id(ci.getId())
                .pdfName(ci.getPdfName())
                .score(ci.getScore())
                .keywords(keywords)
                .aiSummary(ci.getAiSummary())
                .build();
    }

    private List<String> parseKeywordString(String s) {
        if (s == null || s.isBlank()) return null;
        return Arrays.asList(s.split("\\s*,\\s*"));
    }

    private String safeName(String name) {
        if (name == null) return "unknown.pdf";
        // DB 컬럼 길이(45)에 맞춰 자르기
        if (name.length() > 45) {
            // 확장자 보존 간단 처리
            int dot = name.lastIndexOf('.');
            String ext = (dot > -1) ? name.substring(dot) : "";
            String base = name.substring(0, Math.max(0, 45 - ext.length()));
            return base + ext;
        }
        return name;
    }

    private String trimToLength(String s, int max) {
        if (s == null) return null;
        // 멀티바이트 안전 잘라내기(간단 버전)
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= max) return s;
        // 너무 복잡하게 안 가고, 문자 단위로 줄이기
        String truncated = s;
        while (truncated.getBytes(StandardCharsets.UTF_8).length > max) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        return truncated;
    }
}
