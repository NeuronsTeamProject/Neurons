package com.example.resume.resume.repository;

import com.example.resume.resume.entity.HighlightSpan;
import com.example.resume.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HighlightSpanRepository extends JpaRepository<HighlightSpan, Long> {
    List<HighlightSpan> findByResume(Resume resume);
}
/*
역할: 특정 이력서에 속한 하이라이트 목록 조회(findByResume).
어디서: 상세 조회 시 detail()에서 호출해 하이라이트 DTO로 변환.*/