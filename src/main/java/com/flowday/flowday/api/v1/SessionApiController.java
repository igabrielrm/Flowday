package com.flowday.flowday.api.v1;

import com.flowday.flowday.api.ApiAuthHelper;
import com.flowday.flowday.api.dto.ApiResponse;
import com.flowday.flowday.api.dto.UsuarioDto;
import com.flowday.flowday.model.Usuario;
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
