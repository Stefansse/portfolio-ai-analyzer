package com.example.analyticsservice.model.mapper;


import com.example.analyticsservice.model.Analytics;
import com.example.analyticsservice.model.dto.AnalyticsDTO;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsMapper {

    public Analytics toEntity(AnalyticsDTO dto) {
        return Analytics.builder()
                .userId(dto.getUserId())
                .resumeId(dto.getResumeId())
                .weakSkills(dto.getWeakSkills())
                .strongSkills(dto.getStrongSkills())
                .weakSkillsCount(dto.getWeakSkillsCount())
                .goodSkillsCount(dto.getGoodSkillsCount())
                .matchScore(dto.getMatchScore())
                .filename(dto.getFilename())
                .jobDescription(dto.getJobDescription()) // 🆕 map job description
                .uploadedAt(dto.getUploadedAt())
                .build();
    }

    public AnalyticsDTO toDTO(Analytics entity) {
        return AnalyticsDTO.builder()
                .userId(entity.getUserId())
                .resumeId(entity.getResumeId())
                .weakSkills(entity.getWeakSkills())
                .strongSkills(entity.getStrongSkills())
                .weakSkillsCount(entity.getWeakSkillsCount())
                .goodSkillsCount(entity.getGoodSkillsCount())
                .matchScore(entity.getMatchScore())
                .jobDescription(entity.getJobDescription())
                .filename(entity.getFilename())// 🆕 map job description
                .uploadedAt(entity.getUploadedAt())
                .build();
    }
}
