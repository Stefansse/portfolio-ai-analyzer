package com.example.resumeservice.service;

import com.example.resumeservice.entity.Resume;
import com.example.resumeservice.entity.ResumeDocument;
import com.example.resumeservice.entity.StructuredResume;
import com.example.resumeservice.entity.dto.AnalyticsDTO;
import com.example.resumeservice.entity.dto.ResumeResponseDTO;
import com.example.resumeservice.entity.enumerations.FileType;
import com.example.resumeservice.entity.mapper.ResumeMapper;
import com.example.resumeservice.repo.ResumeRepository;
import com.example.resumeservice.repo.ResumeSearchRepository;
import com.example.resumeservice.repo.StructuredResumeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import io.minio.http.Method;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;
    private final MinioClient minioClient;
    private final ResumeSearchRepository resumeSearchRepository;
    private final ResumeAIService resumeAIService;
    private final StructuredResumeRepository structuredResumeRepository;
    private final WebClient userServiceWebClient;      // to get user info
    private final WebClient analyticsWebClient;        // to send analytics

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.url}")
    private String minioUrl;

    private static final int MAX_CHARS_PER_CHUNK = 7000;

    // =========================
    // Upload Resume (PDF/DOCX)
    // =========================
    @Transactional
    public ResumeResponseDTO uploadResume(MultipartFile file, String jobDescription, String jwtToken) throws Exception {
        long startTime = System.currentTimeMillis();

        // ---------------- Extract filename & extension ----------------
        long stepStart = System.currentTimeMillis();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) throw new RuntimeException("Invalid file");
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toUpperCase();
        FileType fileType;
        try {
            fileType = FileType.valueOf(ext);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unsupported file type: " + ext);
        }
        System.out.printf("Step 1: Filename & extension parsed in %d ms%n", System.currentTimeMillis() - stepStart);

        // ---------------- Extract and clean text ----------------
        stepStart = System.currentTimeMillis();
        String rawContent = cleanText(extractText(file, ext));
        if (rawContent.isBlank()) throw new RuntimeException("No readable text found in resume.");
        System.out.printf("Step 2: Text extracted and cleaned in %d ms%n", System.currentTimeMillis() - stepStart);

        // ---------------- Get userId from JWT ----------------
        stepStart = System.currentTimeMillis();
        Long userId = getUserIdFromToken(jwtToken);
        System.out.printf("Step 3: User ID fetched in %d ms%n", System.currentTimeMillis() - stepStart);

        // ---------------- Evaluate resume against job description ----------------
        stepStart = System.currentTimeMillis();
        String matchEvaluation = null;
        JsonNode matchJson = null;
        if (jobDescription != null && !jobDescription.isBlank()) {
            matchEvaluation = resumeAIService.evaluateResumeMatch(rawContent, jobDescription);
            matchJson = objectMapper.readTree(matchEvaluation);
        }
        System.out.printf("Step 4: Resume evaluated against job description in %d ms%n", System.currentTimeMillis() - stepStart);

        // ---------------- Optimize resume HTML ----------------
        stepStart = System.currentTimeMillis();
        String optimizedHtml;
        if (matchJson != null && matchJson.path("weaknesses").isArray() && matchJson.path("weaknesses").size() > 0) {
            List<String> weaknesses = objectMapper.convertValue(matchJson.path("weaknesses"), List.class);
            optimizedHtml = resumeAIService.expandResumeForWeaknesses(rawContent, weaknesses);
        } else {
            optimizedHtml = resumeAIService.optimizeResumeText(rawContent);
        }
        System.out.printf("Step 5: Resume HTML optimized in %d ms%n", System.currentTimeMillis() - stepStart);

        // ---------------- Generate PDF ----------------
        stepStart = System.currentTimeMillis();
        byte[] optimizedPdfBytes = htmlToPdf(optimizedHtml);
        String optimizedObjectName = "optimized-" + UUID.randomUUID() + "-" +
                originalFilename.replaceAll("\\..*$", ".pdf");
        System.out.printf("Step 6: PDF generated in %d ms%n", System.currentTimeMillis() - stepStart);

        // ---------------- Save Resume entity (objectName only) ----------------
        stepStart = System.currentTimeMillis();
        Resume resume = Resume.builder()
                .filename(originalFilename)
                .objectName(optimizedObjectName)
                .fileType(FileType.PDF)
                .size((long) optimizedPdfBytes.length)
                .uploadedAt(LocalDateTime.now())
                .content(rawContent)
                .userId(userId)
                .jobDescription(jobDescription)
                .build();
        resume = resumeRepository.save(resume);
        System.out.printf("Step 7: Resume saved to DB in %d ms%n", System.currentTimeMillis() - stepStart);

        // ---------------- Send analytics ----------------
        stepStart = System.currentTimeMillis();
        if (matchJson != null) {
            sendAnalytics(userId, resume.getId(), matchJson);
        }
        System.out.printf("Step 8: Analytics sent in %d ms%n", System.currentTimeMillis() - stepStart);

        // ---------------- Extract structured data ----------------
        stepStart = System.currentTimeMillis();
        List<String> chunks = splitText(rawContent, MAX_CHARS_PER_CHUNK);
        StringBuilder combinedStructuredJson = new StringBuilder("{\"chunks\": [");
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            String structuredJson = resumeAIService.extractStructuredData(chunk);
            combinedStructuredJson.append(structuredJson);
            if (i < chunks.size() - 1) combinedStructuredJson.append(",");
        }
        combinedStructuredJson.append("]}");

        try {
            JsonNode json = objectMapper.readTree(combinedStructuredJson.toString());
            JsonNode first = json.path("chunks").isArray() && json.path("chunks").size() > 0
                    ? json.path("chunks").get(0)
                    : json;

            StructuredResume structuredResume = StructuredResume.builder()
                    .resumeId(resume.getId())
                    .userId(userId)
                    .name(first.path("name").asText(null))
                    .email(first.path("email").asText(null))
                    .phone(first.path("phone").asText(null))
                    .skills(first.path("skills").isArray() ? first.path("skills").toString() : first.path("skills").asText(null))
                    .education(first.path("education").isArray() ? first.path("education").toString() : first.path("education").asText(null))
                    .workExperience(first.path("work_experience").isArray() ? first.path("work_experience").toString() : first.path("work_experience").asText(null))
                    .projects(first.path("projects").isArray() ? first.path("projects").toString() : first.path("projects").asText(null))
                    .processedAt(LocalDateTime.now())
                    .build();

            structuredResumeRepository.save(structuredResume);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.printf("Step 9: Structured data extracted and saved in %d ms%n", System.currentTimeMillis() - stepStart);

        // ---------------- Async upload to MinIO and Elasticsearch ----------------
        stepStart = System.currentTimeMillis();
        asyncUploadToMinio(optimizedPdfBytes, optimizedObjectName, "application/pdf");
        asyncIndexToElasticsearch(resume);
        System.out.printf("Step 10: Async upload & indexing triggered in %d ms%n", System.currentTimeMillis() - stepStart);

        // ---------------- Map to DTO and generate presigned URL ----------------
        stepStart = System.currentTimeMillis();
        ResumeResponseDTO response = resumeMapper.toDTO(resume);
        if (matchEvaluation != null) {
            response.setMatchEvaluation(objectMapper.convertValue(matchJson, Map.class));
        }

        // Generate presigned URL instead of static URL
        String presignedUrl = generatePresignedDownloadLink(resume.getId());
        response.setUrl(presignedUrl);
        System.out.printf("Step 11: DTO mapping & presigned URL in %d ms%n", System.currentTimeMillis() - stepStart);

        System.out.printf("Total uploadResume execution time: %d ms%n", System.currentTimeMillis() - startTime);
        return response;
    }



    // =========================
    // Analytics microservice call
    // =========================
    private void sendAnalytics(Long userId, Long resumeId, JsonNode matchJson) {
        try {
            // ✅ Fetch the Resume entity to get jobDescription
            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));
            String jobDescription = resume != null && resume.getJobDescription() != null
                    ? resume.getJobDescription()
                    : "Not specified";

            String filename = resume != null && resume.getFilename() != null
                    ? resume.getFilename()
                    : "Unknown";

            // Convert weaknesses and strengths to List<String>
            List<String> weakSkillsList = objectMapper.convertValue(matchJson.path("weaknesses"), List.class);
            List<String> strongSkillsList = matchJson.has("strengths")
                    ? objectMapper.convertValue(matchJson.path("strengths"), List.class)
                    : List.of();

            // Combine each list into a single comma-separated string
            String weakSkillsCombined = String.join(", ", weakSkillsList);
            String strongSkillsCombined = String.join(", ", strongSkillsList);

            int weakSkillsCount = weakSkillsList != null ? weakSkillsList.size() : 0;
            int goodSkillsCount = strongSkillsList != null ? strongSkillsList.size() : 0;

            Integer matchScore = matchJson.has("match_score")
                    ? Integer.parseInt(matchJson.path("match_score").asText().replace("%", "").trim())
                    : null;

            // ✅ Build DTO with jobDescription from Resume entity
            AnalyticsDTO dto = AnalyticsDTO.builder()
                    .userId(userId)
                    .resumeId(resumeId)
                    .filename(filename)
                    .weakSkills(List.of(weakSkillsCombined))
                    .strongSkills(List.of(strongSkillsCombined))
                    .weakSkillsCount(weakSkillsCount)
                    .goodSkillsCount(goodSkillsCount)
                    .matchScore(matchScore)
                    .jobDescription(jobDescription)
                    .uploadedAt(LocalDateTime.now())
                    .build();



            System.out.println("📄 AnalyticsDTO before send: filename=" + dto.getFilename() + ", jobDescription=" + dto.getJobDescription());


            // ✅ Send to Analytics microservice asynchronously
            analyticsWebClient.post()
                    .uri("/analytics")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnError(Throwable::printStackTrace)
                    .subscribe();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // =========================
    // Helper methods (same as before)
    // =========================
    private byte[] htmlToPdf(String html) throws Exception {
        html = html.replaceAll("(?s)^.*?(?=<html|<body)", "");
        if (!html.contains("<html")) html = "<html><head><meta charset='UTF-8'/></head><body>" + html + "</body></html>";

        Document doc = Jsoup.parse(html);
        doc.outputSettings(new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(doc.html());
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        }
    }

    private String extractText(MultipartFile file, String ext) throws Exception {
        try (InputStream textStream = file.getInputStream()) {
            if (ext.equals("DOCX")) {
                try (XWPFDocument doc = new XWPFDocument(textStream);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    return extractor.getText();
                }
            } else if (ext.equals("PDF")) {
                try (PDDocument pdf = PDDocument.load(textStream)) {
                    return new PDFTextStripper().getText(pdf);
                }
            }
        }
        return "";
    }

    private String cleanText(String text) {
        return text.replaceAll("\\s+", " ")
                .replaceAll("[^\\x20-\\x7E\\p{L}\\p{N}\\p{Punct}\\s]", "")
                .trim();
    }

    private List<String> splitText(String text, int chunkSize) {
        return text.codePoints()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining())
                .lines()
                .collect(Collectors.groupingBy(line -> line.length() / chunkSize))
                .values()
                .stream()
                .map(lines -> String.join(" ", lines))
                .collect(Collectors.toList());
    }

    @Async
    public void asyncUploadToMinio(byte[] fileBytes, String objectName, String contentType) {
        try (InputStream stream = new ByteArrayInputStream(fileBytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(stream, fileBytes.length, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void asyncIndexToElasticsearch(Resume resume) {
        try {
            ResumeDocument index = ResumeDocument.builder()
                    .id(resume.getId().toString())
                    .filename(resume.getFilename())
                    .fileType(resume.getFileType().toString())
                    .url(resume.getUrl())
                    .content(resume.getContent())
                    .uploadedAt(resume.getUploadedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();
            resumeSearchRepository.save(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ResumeResponseDTO> getAllResumes() {
        return resumeRepository.findAll().stream().map(resumeMapper::toDTO).collect(Collectors.toList());
    }

    public List<ResumeDocument> searchResumes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();
        return resumeSearchRepository.searchByContent(keyword);
    }

    public String generatePresignedDownloadLink(Long id) {
        Resume resume = getResumeById(id);
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(resume.getObjectName())
                            .expiry(60 * 10)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate download link: " + e.getMessage(), e);
        }
    }

    public Resume getResumeById(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found with id: " + id));
    }

    @Transactional
    public void deleteResume(Long id) {
        Resume resume = getResumeById(id);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(resume.getObjectName())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO: " + e.getMessage(), e);
        }
        resumeRepository.delete(resume);
        resumeSearchRepository.deleteById(resume.getId().toString());
    }

    private Long getUserIdFromToken(String jwtToken) {
        try {
            Map<String, Object> userInfo = userServiceWebClient.get()
                    .uri("/api/users/me")
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return Long.valueOf(userInfo.get("id").toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get userId from token", e);
        }
    }


    public List<ResumeResponseDTO> getUserResumes(String jwtToken) {
        Long userId = getUserIdFromToken(jwtToken);
        return resumeRepository.findAllByUserIdOrderByUploadedAtDesc(userId)
                .stream()
                .map(resumeMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public String compareResumes(MultipartFile file1, MultipartFile file2, String jobDescription) throws Exception {
        // Extract text from both files
        String ext1 = file1.getOriginalFilename().substring(file1.getOriginalFilename().lastIndexOf(".") + 1).toUpperCase();
        String ext2 = file2.getOriginalFilename().substring(file2.getOriginalFilename().lastIndexOf(".") + 1).toUpperCase();

        String text1 = cleanText(extractText(file1, ext1));
        String text2 = cleanText(extractText(file2, ext2));

        if (text1.isBlank() || text2.isBlank()) throw new RuntimeException("One or both resumes are empty");

        // Call AI comparison
        return resumeAIService.compareTwoResumes(text1, text2, jobDescription);
    }



}
