package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * El login admin vive en la SPA ({@code /app/admin/login}).
 * Este controlador solo mantiene el path oculto {@code /internal/login} como redirección.
 */
@Controller
@RequestMapping("${app.admin.path-prefix:/internal}")
public class InternalAuthController {

    @Autowired
    private AppProperties appProperties;

    @GetMapping("/login")
    public String login(@org.springframework.web.bind.annotation.RequestParam(required = false) String error) {
        if ("admin".equals(error)) {
            return "redirect:/app/admin/login?error=admin";
        }
        return "redirect:/app/admin/login";
    }

    @PostMapping("/login")
    public String legacyPost() {
        return "redirect:" + appProperties.getAdmin().getLoginPath();
    }
}
