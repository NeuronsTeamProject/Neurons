package com.example.resume.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicantResponseDTO {

    private Integer id;          // char_info.id
    private String name;         // pdf_name에서 확장자 제거한 값 등
    private String category;     // 한글 직무명: "프론트엔드", "백엔드", "기획자" 등
    private Integer score;       // AI 점수

    private String location;     // 일단 "부산" 등 기본값
    private String career;       // "신입" 또는 "경력 n년" 등

    private SkillsDTO skills;    // 필수/우대/도구 키워드
    private String analysis;     // ai_summary 그대로
    private String strengths;    // 강점 요약 (ai_summary 재활용)

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillsDTO {
        private List<String> required;   // 필수 기술 키워드
        private List<String> preferred;  // 우대 기술 (지금은 비워두고 싶으면 빈 리스트)
        private List<String> tools;      // 사용 도구 (마찬가지)
    }
}
