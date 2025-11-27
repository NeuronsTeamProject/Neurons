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

@Service
@RequiredArgsConstructor
public class ResumeAiService {

    private final E5Client e5Client;
    private final GptClient gptClient;
    private final CharInfoRepository repository;

    /**
     * 프론트에서 들어온 PDF + jobRole(+ name)을 가지고
     *  1) E5 분석 호출 → score, keyword
     *  2) GPT 요약 호출 → aiSummary
     *  3) DB에 한 번에 저장
     *  4) 저장된 결과를 DTO로 반환
     */
    @Transactional
    public ResumeResponseDTO analyzeAll(MultipartFile file, String jobRole, String name) {
        try {
            // 0) 공통 데이터 준비
            byte[] pdfBytes = file.getBytes();

            // pdfName 결정: 프론트에서 name을 주면 그걸 쓰고, 없으면 파일명 사용
            String pdfName = (name != null && !name.isBlank())
                    ? name
                    : safeName(file.getOriginalFilename());

            // 1) E5 분석 (PDF + 직무)
            E5AnalyzeResponse e5 = e5Client.analyzeResume(file, jobRole);
            Integer score = e5 != null ? e5.getScore() : null;
            String keywordString = e5 != null ? e5.getKeyword() : null;

            // 2) GPT 요약 (PDF만 사용)
            String summary = gptClient.summarizePdf(pdfBytes, pdfName);

            // 3) DB 저장 (한 줄에 모두 저장)
            CharInfo saved = repository.save(CharInfo.builder()
                    .pdfName(pdfName)
                    .role(jobRole)
                    .pdf(pdfBytes)
                    .score(score)
                    .keyword(keywordString)
                    .aiSummary(trimToLength(summary, 300)) // ai_summary 컬럼 길이 맞추기
                    .build());

            // 4) 프론트로 보낼 DTO 구성
            return ResumeResponseDTO.builder()
                    .id(saved.getId())
                    .pdfName(saved.getPdfName())
                    .role(saved.getRole())
                    .score(saved.getScore())
                    .keywords(saved.getKeyword())
                    .aiSummary(saved.getAiSummary())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("이력서 분석 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * (옵션) E5만 따로 돌리고 싶을 때 사용할 수 있는 메서드
     * - 지금 컨트롤러에서는 쓰지 않지만, 나중에 디버깅용으로 쓸 수 있음
     */
    @Transactional
    public ResumeResponseDTO analyzeWithE5(MultipartFile file, String jobRole) {
        try {
            E5AnalyzeResponse e5 = e5Client.analyzeResume(file, jobRole);

            CharInfo saved = repository.save(CharInfo.builder()
                    .pdfName(safeName(file.getOriginalFilename()))
                    .role(jobRole)
                    .pdf(file.getBytes())
                    .score(e5 != null ? e5.getScore() : null)
                    .keyword(e5 != null ? e5.getKeyword() : null)
                    .aiSummary(null)
                    .build());

            return ResumeResponseDTO.builder()
                    .id(saved.getId())
                    .pdfName(saved.getPdfName())
                    .role(saved.getRole())
                    .score(saved.getScore())
                    .keywords(saved.getKeyword())
                    .aiSummary(saved.getAiSummary())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("E5 분석 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * (옵션) GPT 요약만 따로 돌리고 싶을 때 사용할 수 있는 메서드
     */
    @Transactional
    public ResumeResponseDTO summarizeWithGpt(MultipartFile file) {
        try {
            byte[] pdfBytes = file.getBytes();
            String pdfName = safeName(file.getOriginalFilename());

            String summary = gptClient.summarizePdf(pdfBytes, pdfName);

            CharInfo saved = repository.save(CharInfo.builder()
                    .pdfName(pdfName)
                    .role(null)
                    .pdf(pdfBytes)
                    .score(null)
                    .keyword(null)
                    .aiSummary(trimToLength(summary, 300))
                    .build());

            return ResumeResponseDTO.builder()
                    .id(saved.getId())
                    .pdfName(saved.getPdfName())
                    .role(saved.getRole())
                    .score(saved.getScore())
                    .keywords(saved.getKeyword())
                    .aiSummary(saved.getAiSummary())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("GPT 요약 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * id로 저장된 결과 조회
     */
    @Transactional(readOnly = true)
    public ResumeResponseDTO getOne(Integer id) {
        CharInfo ci = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        return ResumeResponseDTO.builder()
                .id(ci.getId())
                .pdfName(ci.getPdfName())
                .role(ci.getRole())
                .score(ci.getScore())
                .keywords(ci.getKeyword())
                .aiSummary(ci.getAiSummary())
                .build();
    }

    /**
     * 파일 이름을 안전하게 정리
     */
    private String safeName(String original) {
        if (original == null) return "resume.pdf";
        String name = original;
        // 윈도우/맥 경로 섞여 있을 수 있으니 마지막 /, \ 뒤만 사용
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash != -1 && slash < name.length() - 1) {
            name = name.substring(slash + 1);
        }
        return name;
    }

    /**
     * UTF-8 기준 바이트 길이로 자르기 (DB 컬럼 길이 맞추려고)
     */
    private String trimToLength(String s, int max) {
        if (s == null) return null;
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= max) return s;

        String truncated = s;
        while (truncated.getBytes(StandardCharsets.UTF_8).length > max) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        return truncated;
    }
}
