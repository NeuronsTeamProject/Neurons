package com.example.resume.score.repository;

import com.example.resume.score.entity.GradeThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeThresholdRepository extends JpaRepository<GradeThreshold, Long> {
    List<GradeThreshold> findAllByOrderByMinScoreDesc();
}
