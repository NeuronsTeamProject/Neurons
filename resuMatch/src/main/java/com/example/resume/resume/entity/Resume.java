package com.example.resume.resume.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)            private String displayName;
    @Column(nullable = false)            private String originalFileName;
    @Column(nullable = false, unique = true)
    private String storageKey;           // S3/MinIO key

    @Lob @Column(nullable = false)       private String extractedText;

    // 점수/등급은 score 모듈 계산 후 update (초기 null 허용)
    private Integer totalScore;
    private String grade;

    @Column(nullable = false)            private LocalDateTime uploadedAt;

    @OneToOne(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CandidateInfo candidateInfo;
}

/*
역할: 업로드한 이력서의 핵심 메타데이터(표시이름, 원본파일명, 저장키, 추출텍스트, 업로드시각, 점수/등급)를 보관하는 루트 엔티티.
어디서: ResumeService.upload()에서 생성/저장, detail()/list()에서 조회되어 DTO로 변환.
*/