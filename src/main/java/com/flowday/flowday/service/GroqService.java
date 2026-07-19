package com.flowday.flowday.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Groq — API gratuita con modelos Llama (https://console.groq.com).
 * Compatible con el formato OpenAI chat/completions.
 */
@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.model:llama-3.3-70b-versatile}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String consultar(String prompt) throws IOException {
        if (!estaConfigurado()) {
            throw new IOException("Groq API key no configurada");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("max_tokens", 1024);
        body.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IOException("Error Groq: " + response.getStatusCode() + " - " + response.getBody());
            }
            String texto = extraerTexto(response.getBody());
            if (texto.isBlank()) {
                throw new IOException("Groq respondió vacío");
            }
            log.info("Groq respondió con modelo: {}", model);
            return texto;
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new IOException("Error Groq: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error al llamar Groq: " + e.getMessage(), e);
        }
    }

    private String extraerTexto(String responseBody) throws IOException {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        JsonNode root = objectMapper.readTree(responseBody);
        return root.path("choices").path(0).path("message").path("content").asText("").trim();
    }
}
