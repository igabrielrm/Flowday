package com.uce.servidorproyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Reenvía rutas del cliente React (history API) al index de la SPA en /app/.
 */
@Controller
public class SpaForwardController {

    @GetMapping({
            "/app",
            "/app/",
            "/app/login",
            "/app/register",
            "/app/forgot-password",
            "/app/reset-password",
            "/app/activities",
            "/app/activities/new",
            "/app/activities/{id}/edit",
            "/app/profile",
            "/app/community",
            "/app/chat",
            "/app/calendar",
            "/app/schedule",
            "/app/admin",
            "/app/admin/login",
            "/app/access-denied"
    })
    public String forwardSpaRoot() {
        return "forward:/app/index.html";
    }
}
