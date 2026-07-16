package com.uce.servidorproyecto.api.v1;

import com.uce.servidorproyecto.api.dto.UsuarioDto;
import com.uce.servidorproyecto.config.MobileAuthProperties;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.security.MobileTokenService;
import com.uce.servidorproyecto.security.MobileOAuthCodeService;
import com.uce.servidorproyecto.security.SecurityUtils;
import com.uce.servidorproyecto.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/mobile-auth")
public class MobileAuthController {

    private final UsuarioService usuarioService;
    private final MobileTokenService tokenService;
    private final MobileAuthProperties properties;
    private final MobileOAuthCodeService oauthCodeService;

    public MobileAuthController(UsuarioService usuarioService, MobileTokenService tokenService,
                                MobileAuthProperties properties, MobileOAuthCodeService oauthCodeService) {
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
        this.properties = properties;
        this.oauthCodeService = oauthCodeService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<Usuario> found = usuarioService.autenticar(request.email().trim(), request.password());
        if (found.isEmpty() || "ADMIN".equals(found.get().getRol())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }
        return ResponseEntity.ok(TokenResponse.from(tokenService.issue(found.get())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            return ResponseEntity.ok(TokenResponse.from(tokenService.rotate(request.refreshToken())));
        } catch (MobileTokenService.InvalidRefreshTokenException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", exception.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        tokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Usuario usuario = SecurityUtils.getCurrentUsuario();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }
        return ResponseEntity.ok(UsuarioDto.from(usuario));
    }

    @GetMapping("/oauth-contract")
    public Map<String, Object> oauthContract() {
        return Map.of(
                "enabled", true,
                "grantType", "authorization_code",
                "pkceRequired", true,
                "callback", properties.getOauthCallback(),
                "status", "El navegador externo devuelve un código de un solo uso a la aplicación"
        );
    }

    @GetMapping("/oauth/{provider}/start")
    public void startOAuth(@org.springframework.web.bind.annotation.PathVariable String provider,
                           @org.springframework.web.bind.annotation.RequestParam String codeChallenge,
                           HttpSession session, HttpServletResponse response) throws IOException {
        if (!Set.of("google", "microsoft").contains(provider)) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Proveedor OAuth no permitido");
            return;
        }
        if (!codeChallenge.matches("[A-Za-z0-9_-]{43,128}")) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "codeChallenge PKCE inválido");
            return;
        }
        session.setAttribute(MobileOAuthCodeService.SESSION_MOBILE_OAUTH, Boolean.TRUE);
        session.setAttribute(MobileOAuthCodeService.SESSION_CODE_CHALLENGE, codeChallenge);
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    @PostMapping("/oauth/exchange")
    public ResponseEntity<?> exchangeOAuth(@Valid @RequestBody OAuthExchangeRequest request) {
        try {
            return ResponseEntity.ok(TokenResponse.from(
                    oauthCodeService.exchange(request.code(), request.codeVerifier())));
        } catch (MobileOAuthCodeService.InvalidOAuthCodeException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", exception.getMessage()));
        }
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 128) String password) {}

    public record RefreshRequest(
            @NotBlank @Size(min = 40, max = 512) String refreshToken) {}

    public record OAuthExchangeRequest(
            @NotBlank @Size(min = 40, max = 512) String code,
            @NotBlank @Size(min = 43, max = 128) String codeVerifier) {}

    public record TokenResponse(
            String tokenType,
            String accessToken,
            String refreshToken,
            long expiresIn,
            UsuarioDto user) {
        static TokenResponse from(MobileTokenService.TokenPair pair) {
            return new TokenResponse("Bearer", pair.accessToken(), pair.refreshToken(),
                    pair.expiresIn(), UsuarioDto.from(pair.usuario()));
        }
    }
}
