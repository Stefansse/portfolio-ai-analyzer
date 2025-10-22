package com.example.resumeservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResumeAIService {

    private WebClient webClient;

    @Value("${azure.openai.endpoint}")
    private String azureEndpoint;

    @Value("${azure.openai.key}")
    private String apiKey;

    @Value("${azure.openai.deployment}")
    private String deploymentName;

    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 5000;

    @PostConstruct
    private void init() {
        webClient = WebClient.builder()
                .baseUrl(azureEndpoint)
                .defaultHeader("api-key", apiKey)
                .build();
    }

    // =====================================================
    // Extract structured resume information as JSON
    // =====================================================
    public String extractStructuredData(String resumeText) {
        return sendChatRequest(
                """
                Extract the following details from this resume and return as valid JSON:
                {
                  "name": "",
                  "email": "",
                  "phone": "",
                  "skills": [],
                  "education": [],
                  "work_experience": [],
                  "projects": []
                }
                Resume text:
                """ + resumeText,
                "You are a professional resume parser that always returns valid JSON. Do not add any extra notes or explanations.",
                0
        );
    }

    // =====================================================
    // Optimize resume and generate clean HTML
    // =====================================================
    public String optimizeResumeText(String resumeText) {
        List<String> chunks = splitText(resumeText, 3000); // split into 3k char chunks
        StringBuilder optimizedHtml = new StringBuilder("<html><head><meta charset='UTF-8'/><style> ... </style></head><body>");

        for (String chunk : chunks) {
            String chunkHtml = sendChatRequest(
                    """
                    You are a professional resume designer.
                    Rewrite this text into XHTML (only the chunk content, valid HTML tags, no notes):
                    """ + chunk,
                    "Produce valid XHTML for PDF, no comments, no notes.",
                    0.7
            );
            optimizedHtml.append(chunkHtml);
        }

        optimizedHtml.append("</body></html>");
        return optimizedHtml.toString();
    }

    public List<String> splitText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        int start = 0;
        int length = text.length();

        while (start < length) {
            int end = Math.min(start + chunkSize, length);

            // Try to avoid cutting words in half
            if (end < length) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            start = end;
        }

        return chunks;
    }
    // =====================================================
    // Optimize resume specifically for a job description
    // =====================================================


    // =====================================================
    // Expand resume for missing skills (weaknesses)
    // =====================================================
    public String expandResumeForWeaknesses(String resumeText, List<String> missingSkills) {
        String skillsSentence = String.join(", ", missingSkills);

        return sendChatRequest(
                """
                You are an expert resume writer.
                Rewrite the following resume into a PDF-ready XHTML document and add experience, projects, or skills to cover these missing areas:
                """ + skillsSentence + """

                ⚠️ Requirements:
                - Keep the output as valid XHTML suitable for PDF generation.
                - Use <h1> for name, <h2> for sections, <ul><li> for lists, <p> for paragraphs.
                - Include inline CSS only if necessary.
                - Do NOT include any notes, explanations, or comments outside the actual resume content.
                - Only return the XHTML.

                Resume text:
                """ + resumeText,
                "You are a professional resume designer who maximizes ATS and job match. Do not include any notes or explanations.",
                0.7
        );
    }

    // =====================================================
    // Evaluate resume match against job description
    // =====================================================
    public String evaluateResumeMatch(String resumeText, String jobDescription) {
        return sendChatRequest(
                """
                You are an expert ATS evaluator.
                Evaluate how well this resume matches the following job description.
                Give a match percentage, a short reasoning, and list key missing or strong skills.

                Resume:
                """ + resumeText + """

                Job Description:
                """ + jobDescription + """

                Respond in structured JSON:
                {
                  "match_score": "85%",
                  "summary": "Excellent match for full-stack roles.",
                  "strengths": ["React", "Spring Boot"],
                  "weaknesses": ["Docker", "AWS"]
                }

                ⚠️ Do NOT include any notes, explanations, or extra text outside this JSON.
                """,
                "You are an expert in HR and technical screening. Only respond with JSON, do not include explanations or notes.",
                0.7
        );
    }

    // =====================================================
    // Common Azure OpenAI chat request method
    // =====================================================
    private String sendChatRequest(String userPrompt, String systemRole, double temperature) {
        return sendWithRetry(userPrompt, systemRole, temperature, 0);
    }

    private String sendWithRetry(String userPrompt, String systemRole, double temperature, int attempt) {
        try {
            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/openai/deployments/{deployment}/chat/completions")
                            .queryParam("api-version", "2024-12-01-preview")
                            .build(deploymentName))
                    .bodyValue(Map.of(
                            "messages", List.of(
                                    Map.of("role", "system", "content", systemRole),
                                    Map.of("role", "user", "content", userPrompt)
                            ),
                            "temperature", temperature
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(60))
                    .map(response -> {
                        var choices = (List<Map<String, Object>>) response.get("choices");
                        if (choices != null && !choices.isEmpty()) {
                            var message = (Map<String, Object>) choices.get(0).get("message");
                            return message.get("content").toString().trim();
                        }
                        return "";
                    })
                    .block();

        } catch (WebClientResponseException.TooManyRequests e) {
            if (attempt < MAX_RETRIES) {
                System.err.println("⚠️ Azure OpenAI 429 Too Many Requests. Retrying in " + (RETRY_DELAY_MS / 1000) + " seconds...");
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
                return sendWithRetry(userPrompt, systemRole, temperature, attempt + 1);
            } else {
                System.err.println("❌ Max retries reached. Returning empty result.");
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }



    // =====================================================
// Compare two resumes
// =====================================================
    public String compareTwoResumes(String resumeText1, String resumeText2, String jobDescription) {
        return sendChatRequest(
                """
                You are an expert ATS evaluator and career coach.
                Compare two resumes and evaluate:
        
                1. Overall match to the job description (if provided)
                2. Strong skills for each resume
                3. Weak skills / missing skills for each resume
                4. Skills overlap and differences
                5. Suggested improvements
                6. Recommend which resume has higher chance of getting the job based on match score and skills
        
                Respond in structured JSON:
                {
                  "resume1": {
                    "match_score": "85%",
                    "strengths": ["React", "Spring Boot"],
                    "weaknesses": ["Docker", "AWS"]
                  },
                  "resume2": {
                    "match_score": "78%",
                    "strengths": ["React", "AWS"],
                    "weaknesses": ["Spring Boot", "Docker"]
                  },
                  "common_skills": ["React"],
                  "differences": {
                    "resume1_only": ["Spring Boot"],
                    "resume2_only": ["AWS"]
                  },
                  "recommendation": "Resume 1 has a higher chance of getting the job."
                }
        
                Resume 1:
                """ + resumeText1 + """

        Resume 2:
        """ + resumeText2 + """

        Job Description:
        """ + (jobDescription != null ? jobDescription : "None") + """

        ⚠️ Only return valid JSON, do not include explanations or notes.
        """,
                "You are an expert in HR, ATS evaluation, and technical screening. Only respond with JSON.",
                0.7
        );
    }

}
