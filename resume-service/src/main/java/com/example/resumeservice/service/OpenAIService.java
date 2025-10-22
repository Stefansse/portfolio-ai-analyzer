package com.example.resumeservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> parseResumeText(String resumeText) throws Exception {
        String prompt = """
                Extract the following fields from this resume text and output as valid JSON:
                - name
                - email
                - phone
                - skills (comma-separated)
                - education (array of objects: school, degree, graduationYear)
                - workExperience (array of objects: company, role, startDate, endDate, responsibilities)
                - projects (array of objects: name, description)

                Resume text:
                %s
                """.formatted(resumeText);

        // Call OpenAI API via WebClient
        WebClient client = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        Map<String, Object> request = Map.of(
                "model", "gpt-4",
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                },
                "temperature", 0
        );

        String response = client.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse OpenAI response JSON to extract the JSON text
        Map<?, ?> json = objectMapper.readValue(response, Map.class);
        String content = ((Map<?, ?>)((Map<?, ?>)((java.util.List<?>)json.get("choices")).get(0)).get("message")).get("content").toString();

        // Convert string JSON into Map
        return objectMapper.readValue(content, Map.class);
    }
}
