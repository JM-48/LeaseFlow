package com.rentify.reviewService.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO para información de usuario desde User Service.
 * Solo contiene los campos necesarios para el Review Service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información del usuario desde User Service")
public class UsuarioDTO {

    @Schema(description = "ID del usuario", example = "1")
    private Long id;

    @Schema(description = "Primer nombre del usuario", example = "Juan")
    private String pnombre;

    @Schema(description = "Primer apellido del usuario", example = "Pérez")
    private String papellido;

    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@email.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "ARRIENDATARIO")
    private String rol;

    @Schema(description = "Estado del usuario", example = "ACTIVO")
    private String estado;
}