package com.flowday.flowday.security;

import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class MobileBearerAuthenticationFilter extends OncePerRequestFilter {

    private final MobileJwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public MobileBearerAuthenticationFilter(MobileJwtService jwtService,
                                            UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null
                || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        String rawToken = authorization.substring(7).trim();
        Optional<Usuario> usuario = jwtService.parseUserId(rawToken)
                .flatMap(usuarioRepository::findById)
                .filter(value -> "ACTIVO".equals(value.getEstado()));
        if (usuario.isEmpty()) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Bearer token inválido o expirado\"}");
            return;
        }

        UsuarioPrincipal principal = new UsuarioPrincipal(usuario.get());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
