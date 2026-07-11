package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.security.SecurityUtils;
import com.uce.servidorproyecto.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String raiz() {
        return "redirect:/app/";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/app/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo,
                                @RequestParam String contrasena,
                                HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioService.autenticar(correo, contrasena);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if ("ADMIN".equals(usuario.getRol())) {
                return "redirect:/internal/login?error=admin";
            }
            SecurityUtils.establishAuthenticatedSession(session, usuario);
            return "redirect:/app/";
        }

        return "redirect:/app/login?error=1";
    }

    @GetMapping("/recuperar-contrasena")
    public String recuperarContrasenaForm() {
        return "redirect:/app/forgot-password";
    }

    @GetMapping("/recuperar-contrasena/nueva")
    public String recuperarContrasenaNueva() {
        return "redirect:/app/reset-password";
    }

    @PostMapping({"/recuperar-contrasena", "/recuperar-contrasena/nueva"})
    public String recuperarContrasenaLegacy() {
        return "redirect:/app/forgot-password";
    }

    @GetMapping("/registro")
    public String registro() {
        return "redirect:/app/register";
    }

    @GetMapping({"/registro/paso1", "/registro/paso2", "/registro/paso3"})
    public String registroLegacy() {
        return "redirect:/app/register";
    }

    @PostMapping({"/registro", "/registro/paso1", "/registro/paso2", "/registro/paso3"})
    public String registroLegacyPost() {
        return "redirect:/app/register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SecurityUtils.clearAuthenticatedSession(session);
        return "redirect:/app/login";
    }
}
