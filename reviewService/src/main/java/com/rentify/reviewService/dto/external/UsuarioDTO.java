package com.rentify.reviewService.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO para información de usuario desde User Service.
 * Solo contiene los campos necesarios para el Review Service.
 *
 * IMPORTANTE: rol y estado son objetos anidados en la respuesta real
 * de userService ({"id":1,"nombre":"ADMIN"}), no strings simples.
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

    @Schema(description = "Información del rol del usuario")
    private RolInfo rol;

    @Schema(description = "Información del estado del usuario")
    private EstadoInfo estado;

    /**
     * Obtiene el nombre del rol directamente, para no romper el código
     * existente que llama a usuario.getRol() esperando un String.
     */
    public String getRolNombre() {
        return rol != null ? rol.getNombre() : null;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolInfo {
        @Schema(description = "ID del rol", example = "1")
        private Long id;

        @Schema(description = "Nombre del rol", example = "ADMIN")
        private String nombre;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadoInfo {
        @Schema(description = "ID del estado", example = "1")
        private Long id;

        @Schema(description = "Nombre del estado", example = "ACTIVO")
        private String nombre;
    }
}