package com.ngduc.aichatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngduc.aichatservice.config.GeminiConfig;
import com.ngduc.aichatservice.dto.ChatRequest;
import com.ngduc.aichatservice.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {

    @Autowired
    private GeminiConfig geminiConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatResponse chat(ChatRequest request) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiConfig.getModel() + ":generateContent?key=" + geminiConfig.getApiKey();

        Map<String, Object> message = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", request.getQuestion())))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(message, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
            String answer = parts.get(0).get("text");

            return new ChatResponse(answer);
        } catch (Exception e) {
            return new ChatResponse("Lỗi: " + e.getMessage());
        }
    }
}