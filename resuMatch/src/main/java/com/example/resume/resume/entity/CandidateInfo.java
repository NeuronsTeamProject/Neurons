package com.example.resume.resume.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidate_info")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateInfo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String address;
    private String school;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    private Resume resume;
}
/*
역할: 지원자 기본정보(이름/전화/주소/학교)를 Resume와 1:1로 보관.
업로드 시 사용자가 입력하면 ResumeService.upload()가 생성해 resume에 연결, 상세 조회에서 DTO로 변환.
*/