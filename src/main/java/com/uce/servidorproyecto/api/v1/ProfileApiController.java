package com.uce.servidorproyecto.api.v1;

import com.uce.servidorproyecto.api.ApiAuthHelper;
import com.uce.servidorproyecto.api.dto.ApiResponse;
import com.uce.servidorproyecto.api.dto.ChangePasswordRequest;
import com.uce.servidorproyecto.api.dto.ChangeThemeRequest;
import com.uce.servidorproyecto.api.dto.ProfileDto;
import com.uce.servidorproyecto.api.dto.UpdateProfileRequest;
import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.security.SecurityUtils;
import com.uce.servidorproyecto.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "Perfil del usuario autenticado")
public class ProfileApiController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Obtener perfil completo")
    public ApiResponse<ProfileDto> get(WebRequest request, HttpSession session) {
        Usuario usuario = requireFreshUser(request, session);
        if (usuario == null) return ApiResponse.failure("No autenticado");
        return ApiResponse.success(ProfileDto.from(usuario));
    }

    @PatchMapping
    @Operation(summary = "Actualizar datos del perfil")
    public ApiResponse<ProfileDto> update(@Valid @RequestBody UpdateProfileRequest body,
                                          WebRequest request,
                                          HttpSession session) {
        Usuario sesion = ApiAuthHelper.requireUser(request);
        if (sesion == null) return ApiResponse.failure("No autenticado");

        if (body.nombre() == null || body.nombre().isBlank()) {
            return ApiResponse.failure("El nombre es obligatorio.");
        }
        if (body.telefono() != null && !body.telefono().isBlank() && !usuarioService.telefonoValido(body.telefono())) {
            return ApiResponse.failure("El teléfono debe tener exactamente 10 dígitos numéricos.");
        }
        if (body.telefonoEmergencia() != null && !body.telefonoEmergencia().isBlank()
                && !usuarioService.telefonoValido(body.telefonoEmergencia())) {
            return ApiResponse.failure("El teléfono de emergencia debe tener 10 dígitos numéricos.");
        }

        usuarioService.actualizarPerfil(
                sesion.getId(),
                body.nombre().trim(),
                blankToNull(body.telefono()),
                body.fechaNacimiento(),
                body.genero()
        );

        if (body.nombreEmergencia() != null || body.telefonoEmergencia() != null || body.relacionEmergencia() != null) {
            usuarioService.actualizarEmergencia(
                    sesion.getId(),
                    body.nombreEmergencia() != null ? body.nombreEmergencia().trim() : null,
                    blankToNull(body.telefonoEmergencia()),
                    body.relacionEmergencia()
            );
        }

        Usuario actualizado = syncSession(session, sesion.getId());
        return ApiResponse.success(ProfileDto.from(actualizado));
    }

    @PostMapping("/password")
    @Operation(summary = "Cambiar contraseña")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest body,
                                            WebRequest request) {
        Usuario sesion = ApiAuthHelper.requireUser(request);
        if (sesion == null) return ApiResponse.failure("No autenticado");

        if (!body.contrasenaNueva().equals(body.contrasenaConfirmacion())) {
            return ApiResponse.failure("La nueva contraseña y la confirmación no coinciden.");
        }

        String error = usuarioService.cambiarContrasena(
                sesion.getId(), body.contrasenaActual(), body.contrasenaNueva());
        if (error != null) {
            return ApiResponse.failure(error);
        }
        return ApiResponse.success(null);
    }

    @PatchMapping("/theme")
    @Operation(summary = "Cambiar tema visual")
    public ApiResponse<ProfileDto> changeTheme(@Valid @RequestBody ChangeThemeRequest body,
                                               WebRequest request,
                                               HttpSession session) {
        Usuario sesion = ApiAuthHelper.requireUser(request);
        if (sesion == null) return ApiResponse.failure("No autenticado");

        String tema = body.tema().trim();
        if (!"dark".equals(tema) && !"light".equals(tema)) {
            return ApiResponse.failure("Tema no válido. Usa dark o light.");
        }

        usuarioService.cambiarTema(sesion.getId(), tema);
        Usuario actualizado = syncSession(session, sesion.getId());
        return ApiResponse.success(ProfileDto.from(actualizado));
    }

    @PostMapping("/photo")
    @Operation(summary = "Subir foto de perfil")
    public ApiResponse<ProfileDto> uploadPhoto(@RequestParam("foto") MultipartFile foto,
                                             WebRequest request,
                                             HttpSession session) {
        Usuario sesion = ApiAuthHelper.requireUser(request);
        if (sesion == null) return ApiResponse.failure("No autenticado");

        if (foto == null || foto.isEmpty()) {
            return ApiResponse.failure("Selecciona una imagen para subir.");
        }

        String contentType = foto.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.failure("Solo se permiten archivos de imagen.");
        }
        if (foto.getSize() > 5 * 1024 * 1024) {
            return ApiResponse.failure("La imagen no puede superar 5 MB.");
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
            Usuario actualizado = syncSession(session, sesion.getId());
            return ApiResponse.success(ProfileDto.from(actualizado));
        } catch (Exception e) {
            return ApiResponse.failure("Error al subir la foto: " + e.getMessage());
        }
    }

    private Usuario requireFreshUser(WebRequest request, HttpSession session) {
        Usuario sesion = ApiAuthHelper.requireUser(request);
        if (sesion == null) return null;
        return syncSession(session, sesion.getId());
    }

    private Usuario syncSession(HttpSession session, Long userId) {
        Usuario actualizado = usuarioService.buscarPorId(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (session != null) {
            session.setAttribute(SecurityUtils.SESSION_USUARIO, actualizado);
        }
        return actualizado;
    }

    private static String blankToNull(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}
