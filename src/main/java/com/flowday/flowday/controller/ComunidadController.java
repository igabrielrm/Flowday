package com.flowday.flowday.controller;

import com.flowday.flowday.model.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/comunidad")
public class ComunidadController {

    @GetMapping
    public String comunidad(WebRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuarioLogueado", WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/app/login";
        return "redirect:/app/community";
    }
}
