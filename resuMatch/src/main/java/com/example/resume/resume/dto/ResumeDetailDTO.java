package com.example.resume.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ResumeDetailDTO {
    private Long id;
    private String displayName;
    private String originalFileName;
    private String storageKey;
    private String extractedText;          // PDF → 텍스트
    private CandidateInfoDTO candidate;    // 지원자 정보
    private List<HighlightSpanDTO> highlights; // 키워드 매칭 위치 (있으면)
    private Integer totalScore;            // 점수 모듈 계산 결과 (없으면 null)
    private String grade;                  // 등급 (없으면 null)
    private LocalDateTime uploadedAt;
}
/*
역할: 대시보드 상세 화면에서 필요한 모든 정보(본문 텍스트, 인적사항, 하이라이트, 점수/등급, 시각)를 한 번에 전달.
어디서: ResumeService.detail()에서 엔티티들을 묶어 빌드, ResumeController.detail() 응답.
*/