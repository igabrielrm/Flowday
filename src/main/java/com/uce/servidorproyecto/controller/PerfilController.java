package com.uce.servidorproyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PerfilController {

    @GetMapping("/perfil")
    public String perfil() {
        return "redirect:/app/profile";
    }

    @GetMapping("/perfil/emergencia")
    public String emergencia() {
        return "redirect:/app/profile";
    }

    @PostMapping({"/perfil/actualizar", "/perfil/tema", "/perfil/contrasena", "/perfil/foto"})
    public String legacyPost() {
        return "redirect:/app/profile";
    }
}
