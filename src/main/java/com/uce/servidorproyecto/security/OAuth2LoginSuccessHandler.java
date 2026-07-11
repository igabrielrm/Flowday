package com.uce.servidorproyecto.security;

import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = extractEmail(oauthUser);
        if (email == null || email.isBlank()) {
            response.sendRedirect("/app/login?error=oauth_email");
            return;
        }

        try {
            String nombre = oauthUser.getAttribute("name");
            Usuario usuario = usuarioService.resolverOAuth(email, nombre);
            HttpSession session = request.getSession(true);
            SecurityUtils.establishAuthenticatedSession(session, usuario);
            response.sendRedirect("/app/");
        } catch (IllegalStateException ex) {
            String msg = URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("/app/login?error=oauth_denied&msg=" + msg);
        }
    }

    private String extractEmail(OAuth2User user) {
        String email = user.getAttribute("email");
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        String preferred = user.getAttribute("preferred_username");
        if (preferred != null && preferred.contains("@")) {
            return preferred.trim();
        }
        String upn = user.getAttribute("upn");
        if (upn != null && upn.contains("@")) {
            return upn.trim();
        }
        return null;
    }
}
