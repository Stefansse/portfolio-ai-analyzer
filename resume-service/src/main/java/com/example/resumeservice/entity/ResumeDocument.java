package com.example.resumeservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Document(indexName = "resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeDocument {

    @Id
    private String id;

    private String filename;
    private String fileType;
    private String content; // extracted text
    private String url;
    private long uploadedAt;

}
