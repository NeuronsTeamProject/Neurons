package com.example.resume.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "char_info")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CharInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pdf_name", nullable = false, length = 45)
    private String pdfName;

    @Column(name = "job_role")
    private String role;

    @Lob
    @Column(name = "pdf")
    private byte[] pdf; // LONGBLOB

    @Column(name = "score")
    private Integer score; // INT

    @Column(name = "keyword", length = 255)
    private String keyword; // 콤마로 join해서 저장

    @Column(name = "ai_summary", length = 300)
    private String aiSummary;
}
