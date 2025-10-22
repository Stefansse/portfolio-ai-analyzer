package com.example.analyticsservice.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsDTO {
    private Long userId;
    private Long resumeId;
    private List<String> weakSkills;
    private List<String> strongSkills;
    private Integer weakSkillsCount;
    private Integer goodSkillsCount;
    private Integer matchScore;
    private LocalDateTime uploadedAt;
    private String jobDescription;
    private String filename;
}
