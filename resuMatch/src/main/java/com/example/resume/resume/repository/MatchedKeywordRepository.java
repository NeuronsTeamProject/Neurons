package com.example.resume.resume.repository;

import com.example.resume.resume.entity.MatchedKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchedKeywordRepository extends JpaRepository<MatchedKeyword, Long> {
    List<MatchedKeyword> findByResumeId(Long resumeId);
}
