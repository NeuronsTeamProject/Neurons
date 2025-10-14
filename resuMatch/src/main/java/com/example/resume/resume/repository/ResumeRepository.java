package com.example.resume.resume.repository;

import com.example.resume.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
/*
역할: Resume 엔티티 기본 CRUD.
어디서: 업로드 저장(save), 목록/상세 조회(findAll/findById)에서 사용.
*/