package com.example.analyticsservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillProgressDTO {
    private String skillName;
    private int score; // e.g. 1–10
    private LocalDateTime uploadDate;
}
