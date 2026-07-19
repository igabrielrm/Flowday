package com.flowday.flowday.security;

import com.flowday.flowday.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public final class SecurityUtils {

    public static final String SESSION_USUARIO = "usuarioLogueado";
    public static final int PASSWORD_MIN_LENGTH = 8;

    private SecurityUtils() {
    }

    public static void establishAuthenticatedSession(HttpSession session, Usuario usuario) {
        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        session.setAttribute(SESSION_USUARIO, usuario);
    }

    public static void clearAuthenticatedSession(HttpSession session) {
        SecurityContextHolder.clearContext();
        if (session != null) {
            session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            session.removeAttribute(SESSION_USUARIO);
            session.invalidate();
        }
    }

    public static Usuario getCurrentUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getUsuario();
        }
        return null;
    }

    public static boolean isPasswordStrongEnough(String password) {
        return password != null && password.length() >= PASSWORD_MIN_LENGTH;
    }
}
