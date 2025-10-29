package com.example.resume.resume.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="resume")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Resume {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private String displayName;
    @Column(nullable=false) private String originalFileName;
    @Column(nullable=false, unique = true) private String storageKey;

    @Lob @Column(nullable=false) private String extractedText;

    // 점수/등급: score 모듈에서 채움
    private Integer totalScore;
    private String grade;

    // 하단 종합 총평
    @Lob @Column(columnDefinition = "LONGTEXT")
    private String summaryComment;

    @Column(nullable=false) private LocalDateTime uploadedAt;

    @OneToOne(mappedBy="resume", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    private CandidateInfo candidateInfo;

    // 우측 "매칭 키워드" (필수기술/우대역량/사용도구)
    @OneToMany(mappedBy="resume", cascade=CascadeType.ALL, orphanRemoval=true)
    @Builder.Default
    private List<MatchedKeyword> matchedKeywords = new ArrayList<>();
}


/*
역할: 업로드한 이력서의 핵심 메타데이터(표시이름, 원본파일명, 저장키, 추출텍스트, 업로드시각, 점수/등급)를 보관하는 루트 엔티티.
어디서: ResumeService.upload()에서 생성/저장, detail()/list()에서 조회되어 DTO로 변환.
*/