package com.example.resumeservice.controller;

import com.example.resumeservice.entity.Resume;
import com.example.resumeservice.entity.ResumeDocument;
import com.example.resumeservice.entity.dto.ResumeResponseDTO;
import com.example.resumeservice.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @Autowired
    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeResponseDTO uploadResume(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "jobDescription", required = false) String jobDescription,
            @RequestHeader("Authorization") String authorizationHeader // <-- JWT here
    ) throws Exception {
        // Extract token from header
        String token = authorizationHeader.replace("Bearer ", "").trim();

        return resumeService.uploadResume(file, jobDescription, token);
    }


    @GetMapping
    public ResponseEntity<List<ResumeResponseDTO>> getAllResumes() {
        List<ResumeResponseDTO> resumes = resumeService.getAllResumes();
        return ResponseEntity.ok(resumes);
    }


    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadResume(@PathVariable Long id) {
        try {
            String presignedUrl = resumeService.generatePresignedDownloadLink(id);
            // Return the URL in a structured JSON for now
            return ResponseEntity.ok(Map.of(
                    "downloadUrl", presignedUrl,
                    "filename", resumeService.getResumeById(id).getFilename()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteResume(@PathVariable Long id) {
        try {
            resumeService.deleteResume(id);
            return ResponseEntity.ok("Resume deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public List<ResumeDocument> search(@RequestParam String q) {
        return resumeService.searchResumes(q);
    }


    @GetMapping("/user")
    public List<ResumeResponseDTO> getUserResumes(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        List<ResumeResponseDTO> resumes = resumeService.getUserResumes(token);

        // Generate presigned URLs for download
        for (ResumeResponseDTO resume : resumes) {
            String presignedUrl = resumeService.generatePresignedDownloadLink(resume.getId());
            resume.setUrl(presignedUrl);
        }

        return resumes;
    }


    @PostMapping("/compare-resumes")
    public ResponseEntity<String> compareResumes(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam("file2") MultipartFile file2,
            @RequestParam(value = "jobDescription", required = false) String jobDescription) {
        try {
            String resultJson = resumeService.compareResumes(file1, file2, jobDescription);
            return ResponseEntity.ok(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}