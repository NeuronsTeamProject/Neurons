package com.example.resume.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HighlightSpanDTO {
    private String section;   // 예: "경력", "프로젝트"
    private int startOffset;
    private int endOffset;
    private String tag;       // 매칭된 태그/키워드
}
/*
역할: ResumeDetailDTO 내부에서 지원자 정보, 하이라이트 정보를 표현하는 서브 DTO.
어디서: detail()에서 엔티티를 DTO로 변환해 채움.*/