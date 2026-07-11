package com.uce.servidorproyecto.security;

import com.uce.servidorproyecto.model.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SessionUsuarioSyncFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
            Usuario usuario = principal.getUsuario();
            if (session != null) {
                session.setAttribute(SecurityUtils.SESSION_USUARIO, usuario);
                touchUltimoAcceso(usuario);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void touchUltimoAcceso(Usuario usuario) {
        if (usuario.getUltimoAcceso() == null
                || usuario.getUltimoAcceso().isBefore(LocalDateTime.now().minusMinutes(5))) {
            usuario.setUltimoAcceso(LocalDateTime.now());
        }
    }
}
