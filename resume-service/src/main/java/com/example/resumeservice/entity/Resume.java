package com.example.resumeservice.entity;

import com.example.resumeservice.entity.enumerations.FileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private String url; // Local path or S3 URL

    private Long size;

    private LocalDateTime uploadedAt;

    private String objectName; // actual object name in MinIO

    @Column(columnDefinition = "TEXT")
    private String content;


    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

}
