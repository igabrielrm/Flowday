package com.flowday.flowday.security;

import com.flowday.flowday.config.MobileAuthProperties;
import com.flowday.flowday.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class MobileJwtService {

    private final MobileAuthProperties properties;
    private final SecretKey key;

    public MobileJwtService(MobileAuthProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Usuario usuario) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getCorreo())
                .claim("role", usuario.getRol())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getAccessTtl())))
                .signWith(key)
                .compact();
    }

    public Optional<Long> parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(Long.parseLong(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public long accessExpiresInSeconds() {
        return properties.getAccessTtl().toSeconds();
    }
}
