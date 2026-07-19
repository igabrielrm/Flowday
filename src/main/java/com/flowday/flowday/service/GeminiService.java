package com.flowday.flowday.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String apiUrl;

    private static final String API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String[] MODELOS_FALLBACK = {
            "gemini-2.0-flash-lite",
            "gemini-1.5-flash-latest",
            "gemini-2.0-flash",
            "gemini-2.5-flash"
    };

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String consultar(String prompt) throws IOException {
        if (!estaConfigurado()) {
            throw new IOException("Gemini API key no configurada");
        }

        List<String> modelos = modelosAProbar();
        IOException ultimoError = null;

        for (String modelo : modelos) {
            try {
                String respuesta = consultarModelo(modelo, prompt);
                log.info("Gemini respondió con modelo: {}", modelo);
                return respuesta;
            } catch (IOException e) {
                ultimoError = e;
                String msg = e.getMessage() != null ? e.getMessage() : "";
                log.warn("Modelo Gemini {} no disponible: {}", modelo, msg);
                if (!debeProbarSiguienteModelo(msg)) {
                    throw e;
                }
            }
        }

        throw ultimoError != null ? ultimoError : new IOException("Ningún modelo Gemini disponible");
    }

    private boolean debeProbarSiguienteModelo(String msg) {
        if (msg == null || msg.isBlank()) return false;
        String lower = msg.toLowerCase();
        return lower.contains("404")
                || lower.contains("not_found")
                || lower.contains("not found")
                || lower.contains("429")
                || lower.contains("resource_exhausted")
                || lower.contains("quota")
                || lower.contains("too_many_requests");
    }

    private List<String> modelosAProbar() {
        List<String> modelos = new java.util.ArrayList<>();
        String configurado = extraerModeloDeUrl(apiUrl);
        if (configurado != null && !configurado.isBlank()) {
            modelos.add(configurado);
        }
        for (String m : MODELOS_FALLBACK) {
            if (!modelos.contains(m)) {
                modelos.add(m);
            }
        }
        return modelos;
    }

    private String extraerModeloDeUrl(String url) {
        if (url == null || !url.contains("/models/")) return null;
        String parte = url.substring(url.indexOf("/models/") + 8);
        if (parte.contains(":")) {
            return parte.substring(0, parte.indexOf(':'));
        }
        return parte;
    }

    private String consultarModelo(String modelo, String prompt) throws IOException {
        String url = UriComponentsBuilder
                .fromHttpUrl(API_BASE + modelo + ":generateContent")
                .queryParam("key", apiKey)
                .toUriString();

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> generationConfig = Map.of("maxOutputTokens", 1024);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", List.of(content));
        body.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IOException("Error Gemini (" + modelo + "): " + response.getStatusCode() + " - " + response.getBody());
            }
            String texto = extraerTexto(response.getBody());
            if (texto.isBlank()) {
                throw new IOException("Gemini (" + modelo + ") respondió vacío");
            }
            return texto;
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new IOException("Error Gemini (" + modelo + "): " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error al llamar Gemini (" + modelo + "): " + e.getMessage(), e);
        }
    }

    public String extraerTexto(String responseBody) throws IOException {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray()) {
            return responseBody;
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode part : parts) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(part.path("text").asText(""));
        }
        return sb.toString().trim();
    }
}
