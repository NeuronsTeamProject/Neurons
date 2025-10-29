package com.example.resume.score.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Keyword {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="competency_id")
    private Competency competency;

    @Column(nullable=false)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private MatchType matchType; // EXACT | REGEX

    @Builder.Default
    private int weight = 1;

    @Builder.Default
    private boolean active = true;
}
