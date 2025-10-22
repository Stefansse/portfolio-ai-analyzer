package com.example.resumeservice.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StructuredResumeDTO {
    private Long id;
    private Long resumeId;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String skills;
    private String education;
    private String workExperience;
    private String projects;
    private LocalDateTime processedAt;
}
