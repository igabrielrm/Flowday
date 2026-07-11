package com.uce.servidorproyecto.api.v1;

import com.uce.servidorproyecto.api.ApiAuthHelper;
import com.uce.servidorproyecto.api.dto.ApiResponse;
import com.uce.servidorproyecto.api.dto.UsuarioDto;
import com.uce.servidorproyecto.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
@RequestMapping("/api/v1/session")
@Tag(name = "Session", description = "Sesión del usuario autenticado")
public class SessionApiController {

    @GetMapping("/me")
    @Operation(summary = "Usuario autenticado actual")
    public ApiResponse<UsuarioDto> me(WebRequest request) {
        Usuario usuario = ApiAuthHelper.requireUser(request);
        if (usuario == null) {
            return ApiResponse.failure("No autenticado");
        }
        return ApiResponse.success(UsuarioDto.from(usuario));
    }
}
