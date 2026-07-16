package com.uce.servidorproyecto.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 15;
    private static final long WINDOW_SECONDS = 900;

    private final Map<String, AttemptWindow> attemptsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!isRateLimitedPost(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);
        AttemptWindow window = attemptsByIp.computeIfAbsent(clientKey, key -> new AttemptWindow());

        synchronized (window) {
            window.resetIfExpired();
            if (window.count.get() >= MAX_ATTEMPTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Demasiados intentos. Espera unos minutos e intenta de nuevo.");
                return;
            }
            window.count.incrementAndGet();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedPost(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return "/login".equals(path)
                || path.endsWith("/login")
                || path.endsWith("/admin-login")
                || path.endsWith("/register")
                || path.endsWith("/forgot-password");
    }

    private String resolveClientKey(HttpServletRequest request) {
        // Spring debe resolver proxies confiables mediante server.forward-headers-strategy;
        // aceptar X-Forwarded-For directamente permitiría evadir el límite falsificando el header.
        return request.getRemoteAddr();
    }

    private static final class AttemptWindow {
        private final AtomicInteger count = new AtomicInteger(0);
        private Instant windowStart = Instant.now();

        void resetIfExpired() {
            if (Instant.now().isAfter(windowStart.plusSeconds(WINDOW_SECONDS))) {
                count.set(0);
                windowStart = Instant.now();
            }
        }
    }
}
