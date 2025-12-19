package com.example.resume.service;

import com.example.resume.dto.ResumeResponseDTO;
import com.example.resume.entity.CharInfo;
import com.example.resume.repository.CharInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeAiService {

    private final E5Client e5Client;
    private final GptClient gptClient;
    private final CharInfoRepository repository;

    @Value("${app.upload.dir:C:/resume_uploads}")
    private String uploadDir;

    @Transactional
    public ResumeResponseDTO analyzeWithE5(MultipartFile file, String jobRole) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                originalName = "resume.pdf";
            }

            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String safeOriginal = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String savedName = timestamp + "_" + UUID.randomUUID() + "_" + safeOriginal;

            Path savedPath = dir.resolve(savedName).normalize();
            if (!savedPath.startsWith(dir)) {
                throw new IllegalStateException("허용되지 않은 파일 경로 생성: " + savedPath);
            }

            file.transferTo(savedPath.toFile());

            // ✅ E5 분석
            var e5 = e5Client.analyzeResume(savedPath.toFile(), jobRole);

            System.out.println("[DEBUG] e5 class = " + (e5 == null ? "null" : e5.getClass().getName()));
            System.out.println("[DEBUG] e5 score = " + (e5 == null ? "null" : e5.getScore()));
            System.out.println("[DEBUG] e5 keyword = " + (e5 == null ? "null" : e5.getKeyword()));

            // ✅ 점수/키워드: E5AnalyzeResponse가 total_sum/matches를 제대로 받으면 여기서 바로 32 나옴
            Integer score = (e5 == null) ? null : e5.getScore();
            String keywordString = (e5 == null) ? "" : e5.getKeyword();

            // 안전장치: null이면 0으로 저장(프론트에서 0으로 보이는 걸 확정적으로 컨트롤)
            if (score == null) score = 0;

            // ✅ GPT 요약
            String aiSummary = null;
            try {
                byte[] pdfBytes = Files.readAllBytes(savedPath);
                aiSummary = gptClient.summarizePdf(pdfBytes, originalName);
                aiSummary = trimToUtf8Bytes(aiSummary, 4000);
            } catch (Exception gptEx) {
                gptEx.printStackTrace();
            }

            String keywordForDb = trimToUtf8Bytes(keywordString, 255);

            CharInfo entity = CharInfo.builder()
                    .pdfName(originalName)
                    .role(jobRole)
                    .pdf(savedPath.toString())
                    .score(score)
                    .keyword(keywordForDb)
                    .aiSummary(aiSummary)
                    .build();

            CharInfo saved = repository.save(entity);

            String pdfUrl = "/api/applicants/" + saved.getId() + "/pdf";

            return ResumeResponseDTO.builder()
                    .id(saved.getId())
                    .pdfName(saved.getPdfName())
                    .role(jobRole)
                    .score(saved.getScore())          // ✅ 이제 여기에 32가 들어감
                    .keyword(saved.getKeyword())
                    .aiSummary(saved.getAiSummary())
                    .pdfUrl(pdfUrl)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("analyzeWithE5 실패: " + e.getMessage(), e);
        }
    }

    private String trimToUtf8Bytes(String s, int maxBytes) {
        if (s == null) return null;
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) return s;

        String truncated = s;
        while (truncated.length() > 0 && truncated.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        return truncated;
    }
}
