package com.uce.servidorproyecto.api;

import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.security.SecurityUtils;
import org.springframework.web.context.request.WebRequest;

public final class ApiAuthHelper {

    private ApiAuthHelper() {}

    public static Usuario requireUser(WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute(
                SecurityUtils.SESSION_USUARIO, WebRequest.SCOPE_SESSION);
        if (usuario == null) {
            usuario = SecurityUtils.getCurrentUsuario();
        }
        return usuario;
    }

    public static Usuario requireAdmin(WebRequest request) {
        Usuario usuario = requireUser(request);
        if (usuario == null || !"ADMIN".equals(usuario.getRol())) {
            return null;
        }
        return usuario;
    }
}
