package com.example.resume.resume.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "highlight_span",
        indexes = { @Index(name = "idx_highlight_resume", columnList = "resume_id") })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HighlightSpan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false) private String section;   // 예: "경력"
    @Column(nullable = false) private int startOffset;
    @Column(nullable = false) private int endOffset;
    @Column(nullable = false) private String tag;       // 매칭 태그
}
/*
역할: 본문 텍스트에서 매칭된 키워드/태그의 위치(섹션, 시작/끝 offset, 태그)를 저장.
어디서: (후속 단계) E5 기반 태깅 결과를 저장할 때 사용, 상세 조회 시 detail()에서 불러와 하이라이트 DTO로 내려줌.*/