package com.flowday.flowday.api.v1;

import com.flowday.flowday.api.dto.ApiResponse;
import com.flowday.flowday.api.dto.ForgotPasswordRequest;
import com.flowday.flowday.api.dto.LoginRequest;
import com.flowday.flowday.api.dto.RegisterRequest;
import com.flowday.flowday.api.dto.ResetPasswordRequest;
import com.flowday.flowday.api.dto.UsuarioDto;
import com.flowday.flowday.config.AppProperties;
import com.flowday.flowday.model.Usuario;
import com.flowday.flowday.security.SecurityUtils;
import com.flowday.flowday.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Autenticación para clientes SPA")
public class AuthApiController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AppProperties appProperties;

    @GetMapping("/oauth-providers")
    @Operation(summary = "Proveedores OAuth disponibles")
    public ApiResponse<List<String>> oauthProviders() {
        List<String> ids = new ArrayList<>();
        if (appProperties.getOauth().hasGoogle()) ids.add("google");
        if (appProperties.getOauth().hasMicrosoft()) ids.add("microsoft");
        return ApiResponse.success(ids);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión (JSON)")
    public ApiResponse<UsuarioDto> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioService.autenticar(request.correo(), request.contrasena());
        if (usuarioOpt.isEmpty()) {
            return ApiResponse.failure("Correo o contraseña incorrectos");
        }
        Usuario usuario = usuarioOpt.get();
        if ("ADMIN".equals(usuario.getRol())) {
            return ApiResponse.failure("Los administradores deben usar el acceso interno autorizado.");
        }
        SecurityUtils.establishAuthenticatedSession(session, usuario);
        return ApiResponse.success(UsuarioDto.from(usuario));
    }

    @PostMapping("/admin-login")
    @Operation(summary = "Iniciar sesión de administrador (JSON)")
    public ApiResponse<UsuarioDto> adminLogin(@Valid @RequestBody LoginRequest request, HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioService.autenticar(request.correo(), request.contrasena());
        if (usuarioOpt.isEmpty()) {
            return ApiResponse.failure("Correo o contraseña incorrectos");
        }
        Usuario usuario = usuarioOpt.get();
        if (!"ADMIN".equals(usuario.getRol())) {
            return ApiResponse.failure("Esta cuenta no tiene permisos de administrador.");
        }
        SecurityUtils.establishAuthenticatedSession(session, usuario);
        return ApiResponse.success(UsuarioDto.from(usuario));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión")
    public ApiResponse<Void> logout(HttpSession session) {
        SecurityUtils.clearAuthenticatedSession(session);
        return ApiResponse.success(null);
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario")
    public ApiResponse<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        if (!usuarioService.correoValido(request.correo())) {
            return ApiResponse.failure("Ingresa un correo electrónico válido");
        }
        if (!SecurityUtils.isPasswordStrongEnough(request.contrasena())) {
            return ApiResponse.failure("La contraseña debe tener al menos "
                    + SecurityUtils.PASSWORD_MIN_LENGTH + " caracteres.");
        }
        if (usuarioService.correoExiste(request.correo())) {
            return ApiResponse.failure("Este correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(request.correo().trim());
        usuario.setContrasena(request.contrasena());
        usuario.setTelefono(request.telefono().trim());
        if (request.fechaNacimiento() != null && !request.fechaNacimiento().isBlank()) {
            usuario.setFechaNacimiento(LocalDate.parse(request.fechaNacimiento()));
        }
        if (request.genero() != null && !request.genero().isBlank()) {
            usuario.setGenero(request.genero());
        }
        usuarioService.registrar(usuario);
        return ApiResponse.success(Map.of("mensaje", "Cuenta creada exitosamente"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Verificar datos para recuperar contraseña")
    public ApiResponse<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                           HttpSession session) {
        String error = usuarioService.verificarRecuperacion(request.correo(), request.telefono());
        if (error != null) return ApiResponse.failure(error);
        usuarioService.buscarPorCorreo(request.correo()).ifPresent(u -> session.setAttribute("resetUserId", u.getId()));
        return ApiResponse.success(Map.of("mensaje", "Verificación correcta"));
    }

    @GetMapping("/reset-password/session")
    @Operation(summary = "Comprobar sesión de recuperación activa")
    public ApiResponse<Map<String, Boolean>> resetSession(HttpSession session) {
        return ApiResponse.success(Map.of("active", session.getAttribute("resetUserId") != null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña")
    public ApiResponse<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                                         HttpSession session) {
        Long userId = (Long) session.getAttribute("resetUserId");
        if (userId == null) {
            return ApiResponse.failure("Sesión de recuperación expirada. Intenta de nuevo.");
        }
        if (!request.contrasenaNueva().equals(request.contrasenaConfirmacion())) {
            return ApiResponse.failure("Las contraseñas no coinciden");
        }
        if (!SecurityUtils.isPasswordStrongEnough(request.contrasenaNueva())) {
            return ApiResponse.failure("La contraseña debe tener al menos "
                    + SecurityUtils.PASSWORD_MIN_LENGTH + " caracteres.");
        }
        usuarioService.restablecerContrasena(userId, request.contrasenaNueva());
        session.removeAttribute("resetUserId");
        return ApiResponse.success(Map.of("mensaje", "Contraseña actualizada"));
    }
}
