package com.example.resume.score.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Competency {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    private String description;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private int weight = 1; // 역량 가중치

    @OneToMany(mappedBy = "competency", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Keyword> keywords = new ArrayList<>();
}
