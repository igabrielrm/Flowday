package com.uce.servidorproyecto.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClaudeService {

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String consultar(String prompt) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", "claude-3-haiku-20240307");
        body.put("max_tokens", 1024);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IOException("Error en Claude API: " + response.getStatusCode() + " - " + response.getBody());
            }
            return extraerTexto(response.getBody());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error al llamar a Claude API: " + e.getMessage(), e);
        }
    }

    public String extraerTexto(String responseBody) throws IOException {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode content = root.path("content");
        if (!content.isArray()) {
            return responseBody;
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode block : content) {
            if ("text".equals(block.path("type").asText())) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(block.path("text").asText(""));
            }
        }
        return sb.length() > 0 ? sb.toString() : responseBody;
    }
}
