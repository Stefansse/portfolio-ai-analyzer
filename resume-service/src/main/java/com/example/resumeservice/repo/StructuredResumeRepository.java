package com.example.resumeservice.repo;

import com.example.resumeservice.entity.StructuredResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StructuredResumeRepository extends JpaRepository<StructuredResume, Long> {

    // Find all structured resumes for a given original resume ID
    List<StructuredResume> findByResumeId(Long resumeId);

    // Optional: find all structured resumes for a specific user
    List<StructuredResume> findByUserId(Long userId);
}