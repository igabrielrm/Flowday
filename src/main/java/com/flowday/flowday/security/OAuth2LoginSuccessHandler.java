package com.flowday.flowday.security;

import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.config.MobileAuthProperties;
import com.flowday.flowday.service.UsuarioService;
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

    @Autowired
    private MobileOAuthCodeService mobileOAuthCodeService;

    @Autowired
    private MobileAuthProperties mobileAuthProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = extractEmail(oauthUser);
        if (email == null || email.isBlank()) {
            redirectError(request, response, "oauth_email");
            return;
        }

        try {
            String nombre = oauthUser.getAttribute("name");
            Usuario usuario = usuarioService.resolverOAuth(email, nombre);
            HttpSession session = request.getSession(true);
            if (Boolean.TRUE.equals(session.getAttribute(MobileOAuthCodeService.SESSION_MOBILE_OAUTH))) {
                session.removeAttribute(MobileOAuthCodeService.SESSION_MOBILE_OAUTH);
                String challenge = (String) session.getAttribute(MobileOAuthCodeService.SESSION_CODE_CHALLENGE);
                session.removeAttribute(MobileOAuthCodeService.SESSION_CODE_CHALLENGE);
                String code = mobileOAuthCodeService.issueCode(usuario, challenge);
                response.sendRedirect(mobileAuthProperties.getOauthCallback()
                        + "?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8));
                return;
            }
            SecurityUtils.establishAuthenticatedSession(session, usuario);
            response.sendRedirect("/app/");
        } catch (IllegalStateException ex) {
            String msg = URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
            redirectError(request, response, "oauth_denied&msg=" + msg);
        }
    }

    private void redirectError(HttpServletRequest request, HttpServletResponse response, String error)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null
                && Boolean.TRUE.equals(session.getAttribute(MobileOAuthCodeService.SESSION_MOBILE_OAUTH))) {
            session.removeAttribute(MobileOAuthCodeService.SESSION_MOBILE_OAUTH);
            session.removeAttribute(MobileOAuthCodeService.SESSION_CODE_CHALLENGE);
            response.sendRedirect(mobileAuthProperties.getOauthCallback() + "?error=" + error);
            return;
        }
        response.sendRedirect("/app/login?error=" + error);
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
