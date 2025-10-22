package com.example.resumeservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "structured_resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StructuredResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long resumeId; // link to original Resume

    private Long userId; // optional, connect to User service later

    private String name;
    private String email;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String skills; // JSON or comma-separated

    @Column(columnDefinition = "TEXT")
    private String education; // JSON string

    @Column(columnDefinition = "TEXT")
    private String workExperience; // JSON string

    @Column(columnDefinition = "TEXT")
    private String projects; // optional

    private LocalDateTime processedAt; // when AI processed this resume
}
