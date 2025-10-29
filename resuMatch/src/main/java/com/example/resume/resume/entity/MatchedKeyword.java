package com.example.resume.resume.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="matched_keyword",
        indexes = @Index(name="idx_mk_resume", columnList="resume_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MatchedKeyword {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="resume_id", nullable=false)
    private Resume resume;

    // "필수기술" / "우대역량" / "사용도구"
    @Column(length=30, nullable=false)
    private String category;

    // React, Node.js, Docker 등
    @Column(length=100, nullable=false)
    private String tag;
}
