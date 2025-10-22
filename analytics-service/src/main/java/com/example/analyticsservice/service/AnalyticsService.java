package com.example.analyticsservice.service;

import com.example.analyticsservice.model.Analytics;
import com.example.analyticsservice.model.dto.AnalyticsDTO;
import com.example.analyticsservice.model.dto.SkillProgressOverviewDTO;
import com.example.analyticsservice.model.mapper.AnalyticsMapper;
import com.example.analyticsservice.model.mapper.SkillProgressMapper;
import com.example.analyticsservice.repo.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final AnalyticsMapper mapper;

    // 🧩 Save analytics from ResumeService
    public AnalyticsDTO saveAnalytics(AnalyticsDTO dto) {
        Analytics entity = mapper.toEntity(dto);
        Analytics saved = analyticsRepository.save(entity);
        return mapper.toDTO(saved);
    }

    // 📦 Get all analytics for a user
    public List<AnalyticsDTO> getAnalyticsByUser(Long userId) {
        return analyticsRepository.findByUserId(userId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // 📦 Get all analytics (for admin/debug)
    public List<AnalyticsDTO> getAllAnalytics() {
        return analyticsRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // 📈 Get skill progress trend for a specific user
    public SkillProgressOverviewDTO getSkillProgressForUser(Long userId) {
        List<Analytics> analyticsList = analyticsRepository.findByUserIdOrderByUploadedAtAsc(userId);
        return SkillProgressMapper.toOverview(analyticsList);
    }

    // 🧮 Get the latest analytics record (e.g., for dashboard summary)
    public AnalyticsDTO getLatestAnalyticsForUser(Long userId) {
        return analyticsRepository.findTopByUserIdOrderByUploadedAtDesc(userId)
                .map(mapper::toDTO)
                .orElse(null);
    }

    // 🕓 Get analytics history count (how many resumes uploaded)
    public long getUserUploadCount(Long userId) {
        return analyticsRepository.countByUserId(userId);
    }

    // 🧠 Compare last two uploads (optional – for insights or chart deltas)
    public SkillProgressOverviewDTO getRecentSkillComparison(Long userId) {
        List<Analytics> analyticsList = analyticsRepository.findTop2ByUserIdOrderByUploadedAtDesc(userId);
        return SkillProgressMapper.toOverview(analyticsList);
    }
}
