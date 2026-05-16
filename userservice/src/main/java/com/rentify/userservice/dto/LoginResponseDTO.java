package com.rentify.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta de login exitoso")
public class LoginResponseDTO {

    @Schema(description = "Mensaje de éxito", example = "Login exitoso")
    private String mensaje;

    @Schema(description = "Información completa del usuario autenticado")
    private UsuarioDTO usuario;
}