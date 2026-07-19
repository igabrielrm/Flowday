package com.flowday.flowday.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class IAProviderService {

    private static final Logger log = LoggerFactory.getLogger(IAProviderService.class);

    /** auto | groq | gemini | claude */
    @Value("${ia.provider:auto}")
    private String provider;

    @Autowired
    private GroqService groqService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ClaudeService claudeService;

    public String consultar(String prompt) throws IOException {
        return switch (provider.toLowerCase()) {
            case "groq" -> consultarGroq(prompt);
            case "gemini" -> consultarGemini(prompt);
            case "claude" -> consultarClaude(prompt);
            default -> consultarAuto(prompt);
        };
    }

    /** Groq → Gemini → Claude (el primero que funcione). */
    private String consultarAuto(String prompt) throws IOException {
        IOException ultimo = null;

        if (groqService.estaConfigurado()) {
            try {
                return groqService.consultar(prompt);
            } catch (Exception e) {
                ultimo = e instanceof IOException io ? io : new IOException(e.getMessage(), e);
                log.warn("Groq no disponible: {}", e.getMessage());
            }
        }

        if (geminiService.estaConfigurado()) {
            try {
                return geminiService.consultar(prompt);
            } catch (Exception e) {
                ultimo = e instanceof IOException io ? io : new IOException(e.getMessage(), e);
                log.warn("Gemini no disponible: {}", e.getMessage());
            }
        }

        try {
            return consultarClaude(prompt);
        } catch (IOException e) {
            if (ultimo != null) {
                throw new IOException("Ningún proveedor IA disponible. Último error: " + ultimo.getMessage(), e);
            }
            throw e;
        }
    }

    private String consultarGroq(String prompt) throws IOException {
        return groqService.consultar(prompt);
    }

    private String consultarGemini(String prompt) throws IOException {
        return geminiService.consultar(prompt);
    }

    private String consultarClaude(String prompt) throws IOException {
        return claudeService.consultar(prompt);
    }
}
