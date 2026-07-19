package com.flowday.flowday.security;

import com.flowday.flowday.model.MobileOAuthCode;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.repository.MobileOAuthCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class MobileOAuthCodeService {

    public static final String SESSION_MOBILE_OAUTH = "FLOWDAY_MOBILE_OAUTH";
    public static final String SESSION_CODE_CHALLENGE = "FLOWDAY_MOBILE_CODE_CHALLENGE";

    private final MobileOAuthCodeRepository repository;
    private final MobileTokenService tokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    public MobileOAuthCodeService(MobileOAuthCodeRepository repository, MobileTokenService tokenService) {
        this.repository = repository;
        this.tokenService = tokenService;
    }

    @Transactional
    public String issueCode(Usuario usuario, String codeChallenge) {
        if (!isValidChallenge(codeChallenge)) throw new InvalidOAuthCodeException();
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        MobileOAuthCode code = new MobileOAuthCode();
        code.setCodeHash(hash(raw));
        code.setUsuario(usuario);
        code.setExpiresAt(Instant.now().plus(Duration.ofMinutes(2)));
        code.setCodeChallenge(codeChallenge);
        repository.save(code);
        return raw;
    }

    @Transactional
    public MobileTokenService.TokenPair exchange(String rawCode, String codeVerifier) {
        MobileOAuthCode code = repository.findByCodeHashForUpdate(hash(rawCode))
                .orElseThrow(InvalidOAuthCodeException::new);
        Instant now = Instant.now();
        if (code.getConsumedAt() != null || !code.getExpiresAt().isAfter(now)
                || !"ACTIVO".equals(code.getUsuario().getEstado())) {
            throw new InvalidOAuthCodeException();
        }
        if (!MessageDigest.isEqual(
                code.getCodeChallenge().getBytes(StandardCharsets.US_ASCII),
                challengeFor(codeVerifier).getBytes(StandardCharsets.US_ASCII))) {
            throw new InvalidOAuthCodeException();
        }
        code.setConsumedAt(now);
        return tokenService.issue(code.getUsuario());
    }

    private String hash(String value) {
        if (value == null || value.isBlank() || value.length() > 512) {
            throw new InvalidOAuthCodeException();
        }
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private String challengeFor(String verifier) {
        if (verifier == null || !verifier.matches("[A-Za-z0-9._~-]{43,128}")) {
            throw new InvalidOAuthCodeException();
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private boolean isValidChallenge(String challenge) {
        return challenge != null && challenge.matches("[A-Za-z0-9_-]{43,128}");
    }

    public static final class InvalidOAuthCodeException extends RuntimeException {
        public InvalidOAuthCodeException() {
            super("Código OAuth inválido o expirado");
        }
    }
}
