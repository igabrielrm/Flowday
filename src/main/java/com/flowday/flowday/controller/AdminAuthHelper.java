package com.flowday.flowday.controller;

import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

public final class AdminAuthHelper {

    private AdminAuthHelper() {}

    public static Usuario requerirAdmin(WebRequest request) {
        Usuario usuario = obtenerAdmin(request);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión expirada");
        }
        return usuario;
    }

    /** @return null si autorizado; redirect si no */
    public static String requerirAdminMvc(WebRequest request, String loginPath) {
        if (obtenerAdmin(request) != null) {
            return null;
        }
        return "redirect:" + loginPath;
    }

    private static Usuario obtenerAdmin(WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute(
                SecurityUtils.SESSION_USUARIO, WebRequest.SCOPE_SESSION);
        if (usuario == null) {
            usuario = SecurityUtils.getCurrentUsuario();
        }
        if (usuario == null || !"ADMIN".equals(usuario.getRol())) {
            return null;
        }
        return usuario;
    }
}
