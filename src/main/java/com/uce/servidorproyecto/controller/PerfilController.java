package com.uce.servidorproyecto.controller;

import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Controller
public class PerfilController {

    private static final String SESSION_USUARIO = "usuarioLogueado";

    @Autowired
    private UsuarioService usuarioService;

    private Usuario refrescarSesion(WebRequest request) {
        Usuario sesion = (Usuario) request.getAttribute(SESSION_USUARIO, WebRequest.SCOPE_SESSION);
        if (sesion == null) return null;
        return usuarioService.buscarPorId(sesion.getId()).orElse(sesion);
    }

    private void guardarEnSesion(HttpSession session, Usuario usuario) {
        if (session != null) {
            session.setAttribute(SESSION_USUARIO, usuario);
        }
    }

    @GetMapping("/perfil")
    public String perfil(WebRequest request, HttpSession session, Model model) {
        Usuario usuario = refrescarSesion(request);
        if (usuario == null) return "redirect:/login";

        guardarEnSesion(session, usuario);
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @GetMapping("/perfil/emergencia")
    public String emergencia(WebRequest request, Model model) {
        Usuario usuario = refrescarSesion(request);
        if (usuario == null) return "redirect:/login";

        model.addAttribute("usuario", usuario);
        return "perfil-emergencia";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam String nombre,
                                   @RequestParam String carrera,
                                   @RequestParam(required = false) String telefono,
                                   @RequestParam(required = false) String fechaNacimiento,
                                   @RequestParam(required = false) String genero,
                                   @RequestParam(required = false) String nombreEmergencia,
                                   @RequestParam(required = false) String telefonoEmergencia,
                                   @RequestParam(required = false) String relacionEmergencia,
                                   WebRequest request,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        Usuario sesion = (Usuario) request.getAttribute(SESSION_USUARIO, WebRequest.SCOPE_SESSION);
        if (sesion == null) return "redirect:/login";

        if (nombre == null || nombre.isBlank()) {
            ra.addFlashAttribute("error", "El nombre es obligatorio.");
            return "redirect:/perfil";
        }
        if (telefono != null && !telefono.isBlank() && !usuarioService.telefonoValido(telefono)) {
            ra.addFlashAttribute("error", "El teléfono debe tener exactamente 10 dígitos numéricos.");
            return "redirect:/perfil";
        }
        if (telefonoEmergencia != null && !telefonoEmergencia.isBlank()
                && !usuarioService.telefonoValido(telefonoEmergencia)) {
            ra.addFlashAttribute("error", "El teléfono de emergencia debe tener 10 dígitos numéricos.");
            return "redirect:/perfil";
        }

        LocalDate nacimiento = null;
        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            nacimiento = LocalDate.parse(fechaNacimiento);
        }

        usuarioService.actualizarPerfil(sesion.getId(), nombre.trim(), carrera,
                telefono != null && !telefono.isBlank() ? telefono.trim() : null,
                nacimiento, genero);

        if (nombreEmergencia != null || telefonoEmergencia != null || relacionEmergencia != null) {
            usuarioService.actualizarEmergencia(sesion.getId(),
                    nombreEmergencia != null ? nombreEmergencia.trim() : null,
                    telefonoEmergencia != null && !telefonoEmergencia.isBlank() ? telefonoEmergencia.trim() : null,
                    relacionEmergencia);
        }

        Usuario actualizado = usuarioService.buscarPorId(sesion.getId()).orElse(sesion);
        guardarEnSesion(session, actualizado);
        ra.addFlashAttribute("exito", "Perfil actualizado correctamente");
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/tema")
    public String cambiarTema(@RequestParam String tema, WebRequest request, RedirectAttributes ra) {
        Usuario usuario = (Usuario) request.getAttribute(SESSION_USUARIO, WebRequest.SCOPE_SESSION);
        if (usuario == null) return "redirect:/login";

        usuarioService.cambiarTema(usuario.getId(), tema);
        ra.addFlashAttribute("exito", "Tema cambiado a " + tema);
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/foto")
    public String subirFoto(@RequestParam("foto") MultipartFile foto, WebRequest request,
                            HttpSession session, RedirectAttributes ra) {
        Usuario sesion = (Usuario) request.getAttribute(SESSION_USUARIO, WebRequest.SCOPE_SESSION);
        if (sesion == null) return "redirect:/login";

        if (foto == null || foto.isEmpty()) {
            ra.addFlashAttribute("error", "Selecciona una imagen para subir.");
            return "redirect:/perfil";
        }

        String contentType = foto.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            ra.addFlashAttribute("error", "Solo se permiten archivos de imagen.");
            return "redirect:/perfil";
        }

        try {
            Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String extension = contentType.replace("image/", ".");
            if ("jpeg".equals(extension.substring(1))) extension = ".jpg";
            String fileName = sesion.getId() + "_" + System.currentTimeMillis() + extension;
            Path path = uploadDir.resolve(fileName);
            Files.write(path, foto.getBytes());

            Usuario usuario = usuarioService.buscarPorId(sesion.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            usuario.setFoto("/uploads/" + fileName);
            usuarioService.guardar(usuario);
            guardarEnSesion(session, usuario);
            ra.addFlashAttribute("exito", "Foto actualizada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al subir la foto: " + e.getMessage());
        }
        return "redirect:/perfil";
    }
}
