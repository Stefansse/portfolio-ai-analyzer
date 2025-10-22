package com.example.analyticsservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillProgressOverviewDTO {
    private Long userId;
    // key: skill name, value: list of skill progress points (sorted by upload time)
    private Map<String, List<SkillProgressDTO>> skillTrends;
}
