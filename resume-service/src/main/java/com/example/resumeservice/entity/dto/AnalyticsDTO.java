package com.example.resumeservice.entity.dto;

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
    private Integer weakSkillsCount;   // new
    private Integer goodSkillsCount;   // new
    private Integer matchScore;        // new
    private LocalDateTime uploadedAt;
    private String jobDescription;
    private String filename;
}
