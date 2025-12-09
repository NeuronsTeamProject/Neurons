package com.example.resume.service;

import com.example.resume.dto.ResumeResponseDTO;
import com.example.resume.entity.CharInfo;
import com.example.resume.repository.CharInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ResumeAiService {

    private final E5Client e5Client;
    private final CharInfoRepository repository;

    /**
     * 프론트에서 업로드한 이력서를 E5로 분석하고,
     * 결과를 DB(char_info)에 저장한 뒤, 요약 DTO를 반환.
     */
    @Transactional
    public ResumeResponseDTO analyze(MultipartFile multipartFile, String jobRole) {
        try {
            // 원본 파일명 (없으면 기본값)
            String originalName = multipartFile.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                originalName = "resume.pdf";
            }

            // 1) MultipartFile → 실제 임시 파일로 저장
            File tempPdf = File.createTempFile("resume-", ".pdf");
            multipartFile.transferTo(tempPdf);   // 여기서 File 타입 사용

            // 2) E5 Python 모델 호출 (File, jobRole)
            E5Client.E5Result e5 = e5Client.analyzeResume(tempPdf, jobRole);

            // 3) DB에 저장할 엔티티 생성
            CharInfo entity = CharInfo.builder()
                    .pdfName(originalName)
                    .role(jobRole)
                    .pdf(multipartFile.getBytes())                 // 바이너리로 저장
                    .score(e5.getScore())                          // E5 점수
                    .keyword(trimToLength(e5.getKeyword(), 500))   // 너무 길면 잘라서 저장
                    // .aiSummary(요약문)  // GPT 요약 붙일 거면 여기 채우면 됨
                    .build();

            CharInfo saved = repository.save(entity);

            // 4) 임시 파일 정리
            tempPdf.deleteOnExit();

            // 5) 프론트로 보낼 DTO 구성
            return ResumeResponseDTO.builder()
                    .id(saved.getId())
                    .pdfName(saved.getPdfName())
                    .role(saved.getRole())
                    .score(saved.getScore())
                    .keywords(saved.getKeyword())
                    .aiSummary(saved.getAiSummary())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("이력서 분석/저장 중 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 단건 조회 (선택적으로 사용)
     */
    @Transactional(readOnly = true)
    public ResumeResponseDTO getOne(Integer id) {
        CharInfo entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이력서 ID: " + id));

        return ResumeResponseDTO.builder()
                .id(entity.getId())
                .pdfName(entity.getPdfName())
                .role(entity.getRole())
                .score(entity.getScore())
                .keywords(entity.getKeyword())
                .aiSummary(entity.getAiSummary())
                .build();
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
