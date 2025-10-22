package com.example.resumeservice.entity.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ResumeUploadDTO {
    private MultipartFile file;
}