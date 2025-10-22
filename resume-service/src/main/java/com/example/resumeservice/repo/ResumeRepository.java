package com.example.resumeservice.repo;


import com.example.resumeservice.entity.Resume;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
     List<Resume> findAllByUserIdOrderByUploadedAtDesc(Long userId);
}