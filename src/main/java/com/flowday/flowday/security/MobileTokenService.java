package com.flowday.flowday.security;

import com.flowday.flowday.config.MobileAuthProperties;
import com.flowday.flowday.model.MobileRefreshToken;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.repository.MobileRefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class MobileTokenService {

    private final MobileRefreshTokenRepository refreshTokenRepository;
    private final MobileJwtService jwtService;
    private final MobileAuthProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public MobileTokenService(MobileRefreshTokenRepository refreshTokenRepository,
                              MobileJwtService jwtService,
                              MobileAuthProperties properties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.properties = properties;
    }

    @Transactional
    public TokenPair issue(Usuario usuario) {
        String rawRefreshToken = newRefreshToken();
        MobileRefreshToken persisted = buildToken(usuario, hash(rawRefreshToken), Instant.now());
        refreshTokenRepository.save(persisted);
        return pair(usuario, rawRefreshToken);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public TokenPair rotate(String rawRefreshToken) {
        String presentedHash = hash(rawRefreshToken);
        MobileRefreshToken current = refreshTokenRepository.findByTokenHashForUpdate(presentedHash)
                .orElseThrow(InvalidRefreshTokenException::new);
        Instant now = Instant.now();

        if (current.getRevokedAt() != null) {
            if (current.getReplacedByHash() != null) {
                refreshTokenRepository.revokeAllForUser(current.getUsuario().getId(), now);
            }
            throw new InvalidRefreshTokenException();
        }
        if (!current.getExpiresAt().isAfter(now)
                || !"ACTIVO".equals(current.getUsuario().getEstado())) {
            current.setRevokedAt(now);
            throw new InvalidRefreshTokenException();
        }

        String replacementRaw = newRefreshToken();
        String replacementHash = hash(replacementRaw);
        current.setRevokedAt(now);
        current.setReplacedByHash(replacementHash);
        refreshTokenRepository.save(buildToken(current.getUsuario(), replacementHash, now));
        return pair(current.getUsuario(), replacementRaw);
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHashForUpdate(hash(rawRefreshToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());
            }
        });
    }

    private TokenPair pair(Usuario usuario, String rawRefreshToken) {
        return new TokenPair(jwtService.createAccessToken(usuario), rawRefreshToken,
                jwtService.accessExpiresInSeconds(), usuario);
    }

    private MobileRefreshToken buildToken(Usuario usuario, String tokenHash, Instant now) {
        MobileRefreshToken token = new MobileRefreshToken();
        token.setUsuario(usuario);
        token.setTokenHash(tokenHash);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(properties.getRefreshTtl()));
        return token;
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String value) {
        if (value == null || value.isBlank() || value.length() > 512) {
            throw new InvalidRefreshTokenException();
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public record TokenPair(String accessToken, String refreshToken, long expiresIn, Usuario usuario) {}

    public static final class InvalidRefreshTokenException extends RuntimeException {
        public InvalidRefreshTokenException() {
            super("Refresh token inválido o expirado");
        }
    }
}
