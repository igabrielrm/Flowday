package com.uce.servidorproyecto.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
public class MobileAuthProperties {

    private static final String DEV_SECRET =
            "dev-only-mobile-jwt-secret-change-before-production-2026";

    private final Environment environment;

    @Value("${mobile.auth.jwt-secret:" + DEV_SECRET + "}")
    private String jwtSecret;

    @Value("${mobile.auth.access-ttl:15m}")
    private Duration accessTtl;

    @Value("${mobile.auth.refresh-ttl:30d}")
    private Duration refreshTtl;

    @Value("${mobile.auth.issuer:flowday-api}")
    private String issuer;

    @Value("${mobile.auth.allowed-origins:https://localhost,capacitor://localhost,http://localhost:5173}")
    private List<String> allowedOrigins;

    @Value("${mobile.auth.oauth-callback:com.uce.flowday://auth/callback}")
    private String oauthCallback;

    public MobileAuthProperties(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    void validate() {
        boolean production = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (jwtSecret == null || jwtSecret.length() < 32
                || (production && DEV_SECRET.equals(jwtSecret))) {
            throw new IllegalStateException(
                    "MOBILE_JWT_SECRET debe configurarse con al menos 32 caracteres en producción");
        }
        if (accessTtl.isNegative() || accessTtl.isZero() || accessTtl.compareTo(Duration.ofHours(1)) > 0) {
            throw new IllegalStateException("mobile.auth.access-ttl debe estar entre 1 segundo y 1 hora");
        }
        if (refreshTtl.compareTo(Duration.ofDays(1)) < 0) {
            throw new IllegalStateException("mobile.auth.refresh-ttl debe ser al menos 1 día");
        }
        if (getAllowedOrigins().isEmpty() || getAllowedOrigins().stream().anyMatch(origin -> origin.contains("*"))) {
            throw new IllegalStateException(
                    "mobile.auth.allowed-origins requiere orígenes exactos y no permite wildcard");
        }
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public Duration getAccessTtl() {
        return accessTtl;
    }

    public Duration getRefreshTtl() {
        return refreshTtl;
    }

    public String getIssuer() {
        return issuer;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins.stream().map(String::trim).filter(value -> !value.isBlank()).toList();
    }

    public String getOauthCallback() {
        return oauthCallback;
    }
}
