package com.eventbooking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYSTEM_PROMPT =
        "You are OP Assistant, a helpful chatbot for the EventSphere department event booking system. " +
        "You help users with questions about events, ticket booking, pricing, venues, and general navigation. " +
        "Keep responses concise, friendly, and relevant to event booking. " +
        "If asked about something unrelated, politely redirect to event-related topics.";

    public String getBotResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Please type a message so I can help you!";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> systemMsg = new LinkedHashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);

            Map<String, Object> userMsg = new LinkedHashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", "llama-3.1-8b-instant");
            body.put("messages", List.of(systemMsg, userMsg));
            body.put("max_tokens", 300);
            body.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Groq API error: " + e.getMessage());
        }

        return "I'm having trouble connecting right now. Please try again shortly or contact support.";
    }
}
