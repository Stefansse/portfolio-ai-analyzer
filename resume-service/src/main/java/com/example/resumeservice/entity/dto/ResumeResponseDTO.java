package com.example.resumeservice.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeResponseDTO {
    private Long id;
    private String filename;
    private String fileType;
    private String url; // maybe a public URL instead of local path
    private Long size;
    private LocalDateTime uploadedAt;
    private String content;
    private Map<String, Object> matchEvaluation; //
    private String jobDescription;



}