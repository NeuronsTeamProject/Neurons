package com.example.resume.score.repository;

import com.example.resume.score.entity.Competency;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetencyRepository extends JpaRepository<Competency, Long> {
    @EntityGraph(attributePaths = "keywords")
    List<Competency> findByActiveTrueOrderByNameAsc();
}
