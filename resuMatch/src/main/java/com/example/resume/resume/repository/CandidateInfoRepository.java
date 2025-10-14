package com.example.resume.resume.repository;

import com.example.resume.resume.entity.CandidateInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateInfoRepository extends JpaRepository<CandidateInfo, Long> {
}
/*
역할: CandidateInfo 기본 CRUD.
어디서: 업로드 시 인적사항이 있으면 save로 저장.*/