package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String raiz() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) Boolean admin, Model model) {
        model.addAttribute("modoAdmin", Boolean.TRUE.equals(admin));
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo,
                                @RequestParam String contrasena,
                                @RequestParam(required = false) Boolean modoAdmin,
                                HttpSession session,
                                Model model) {
        System.out.println("🔐 Intento de login: " + correo);

        Optional<Usuario> usuarioOpt = usuarioService.autenticar(correo, contrasena);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            session.setAttribute("usuarioLogueado", usuario);
            System.out.println("✅ Sesión guardada para: " + usuario.getCorreo());
            return "ADMIN".equals(usuario.getRol()) ? "redirect:/admin/dashboard" : "redirect:/dashboard";
        }

        System.out.println("❌ Falló autenticación para: " + correo);
        model.addAttribute("error", "Correo o contraseña incorrectos");
        model.addAttribute("modoAdmin", Boolean.TRUE.equals(modoAdmin));
        return "login";
    }

    // ===== REGISTRO =====
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro-paso1";
    }

    @PostMapping("/registro/paso1")
    public String procesarPaso1(@RequestParam String nombre,
                                @RequestParam String correo,
                                @RequestParam String contrasena,
                                HttpSession session,
                                Model model) {
        if (!usuarioService.correoValido(correo)) {
            model.addAttribute("error", "Solo se permiten correos institucionales @uce.edu.ec");
            model.addAttribute("usuario", new Usuario());
            return "registro-paso1";
        }
        if (usuarioService.correoExiste(correo)) {
            model.addAttribute("error", "Este correo ya está registrado");
            model.addAttribute("usuario", new Usuario());
            return "registro-paso1";
        }

        Usuario temporal = new Usuario();
        temporal.setNombre(nombre);
        temporal.setCorreo(correo);
        temporal.setContrasena(contrasena);
        session.setAttribute("registroTemporal", temporal);

        return "redirect:/registro/paso2";
    }

    @GetMapping("/registro/paso2")
    public String registroPaso2(HttpSession session, Model model) {
        if (session.getAttribute("registroTemporal") == null) {
            return "redirect:/registro";
        }
        return "registro-paso2";
    }

    @PostMapping("/registro/paso2")
    public String procesarPaso2(@RequestParam String carrera,
                                @RequestParam String telefono,
                                @RequestParam String fechaNacimiento,
                                @RequestParam String genero,
                                HttpSession session,
                                RedirectAttributes ra) {
        Usuario temporal = (Usuario) session.getAttribute("registroTemporal");
        if (temporal == null) return "redirect:/registro";

        if (telefono == null || telefono.isBlank()) {
            ra.addFlashAttribute("error", "El teléfono celular es obligatorio.");
            return "redirect:/registro/paso2";
        }
        if (!usuarioService.telefonoValido(telefono.trim())) {
            ra.addFlashAttribute("error", "El teléfono debe tener exactamente 10 dígitos numéricos (ej: 0991234567).");
            return "redirect:/registro/paso2";
        }

        temporal.setCarrera(carrera);
        temporal.setTelefono(telefono.trim());
        if (!fechaNacimiento.isEmpty()) {
            temporal.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        }
        temporal.setGenero(genero);
        session.setAttribute("registroTemporal", temporal);

        return "redirect:/registro/paso3";
    }

    @GetMapping("/registro/paso3")
    public String registroPaso3(HttpSession session) {
        if (session.getAttribute("registroTemporal") == null) {
            return "redirect:/registro";
        }
        return "registro-paso3";
    }

    @PostMapping("/registro/paso3")
    public String procesarPaso3(@RequestParam String nombreEmergencia,
                                @RequestParam String telefonoEmergencia,
                                @RequestParam String relacionEmergencia,
                                HttpSession session,
                                RedirectAttributes ra) {
        Usuario temporal = (Usuario) session.getAttribute("registroTemporal");
        if (temporal == null) return "redirect:/registro";

        temporal.setNombreEmergencia(nombreEmergencia);
        temporal.setTelefonoEmergencia(telefonoEmergencia);
        temporal.setRelacionEmergencia(relacionEmergencia);

        usuarioService.registrar(temporal);
        session.removeAttribute("registroTemporal");

        ra.addFlashAttribute("exito", "✅ Cuenta creada exitosamente");
        return "redirect:/login?registrado=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}