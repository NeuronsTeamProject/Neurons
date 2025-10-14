package com.example.resume.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResumeListItemDTO {
    private Long id;
    private String displayName;   // 좌측 패널에 보일 이름
    private Integer totalScore;   // 아직 점수 전이면 null
    private String grade;         // 등급(예: A/B/C) — 점수 계산 전이면 null
}
/*
역할: 좌측 목록(리스트 패널)에서 필요한 최소 정보(id, 표시이름, 점수/등급)를 담음.
어디서: ResumeService.list()에서 Resume들을 매핑해 반환, ResumeController.list() 응답.
*/