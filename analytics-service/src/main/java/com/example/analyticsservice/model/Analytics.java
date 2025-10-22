package com.example.analyticsservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long resumeId;

    @ElementCollection
    @CollectionTable(name = "analytics_weak_skills", joinColumns = @JoinColumn(name = "analytics_id"))
    @Column(name = "weak_skill", columnDefinition = "TEXT") // or @Lob
    private List<String> weakSkills;

    @ElementCollection
    @CollectionTable(name = "analytics_strong_skills", joinColumns = @JoinColumn(name = "analytics_id"))
    @Column(name = "strong_skill", columnDefinition = "TEXT") // or @Lob
    private List<String> strongSkills;

    private Integer weakSkillsCount;

    private Integer goodSkillsCount;

    private Integer matchScore;

    private LocalDateTime uploadedAt;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;
    private String filename;
}
