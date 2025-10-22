package com.example.analyticsservice.repo;


import com.example.analyticsservice.model.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {
    List<Analytics> findByUserId(Long userId);

    List<Analytics> findByUserIdOrderByUploadedAtAsc(Long userId);

    Optional<Analytics> findTopByUserIdOrderByUploadedAtDesc(Long userId);

    long countByUserId(Long userId);

    List<Analytics> findTop2ByUserIdOrderByUploadedAtDesc(Long userId);
}
