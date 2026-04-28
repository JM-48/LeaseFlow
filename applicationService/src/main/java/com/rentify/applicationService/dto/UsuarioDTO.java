package com.rentify.applicationService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para información del usuario proveniente del User Service
 * Contiene datos básicos del usuario necesarios para validaciones
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información básica del usuario (desde User Service)")
public class UsuarioDTO {

    @Schema(description = "ID único del usuario", example = "1")
    private Long id;

    @Schema(description = "Primer nombre del usuario", example = "Juan")
    private String pnombre;

    @Schema(description = "Segundo nombre del usuario", example = "Carlos")
    private String snombre;

    @Schema(description = "Primer apellido del usuario", example = "Pérez")
    private String papellido;

    @Schema(description = "Email del usuario", example = "juan.perez@email.com")
    private String email;

    @Schema(description = "Teléfono de contacto", example = "987654321")
    private String ntelefono;

    @Schema(description = "ID del rol del usuario", example = "3")
    private Integer rolId;

    @Schema(description = "Objeto con información del rol")
    private RolInfo rol;

    @Schema(description = "Objeto con información del estado")
    private EstadoInfo estado;

    @Schema(description = "Indica si el usuario es VIP (correo @duoc.cl)", example = "false")
    private Boolean duocVip;

    /**
     * Clase interna para mapear la información del rol
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolInfo {
        @Schema(description = "ID del rol", example = "3")
        private Integer id;

        @Schema(description = "Nombre del rol", example = "ARRIENDATARIO")
        private String nombre;
    }

    /**
     * Clase interna para mapear la información del estado
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadoInfo {
        @Schema(description = "ID del estado", example = "1")
        private Integer id;

        @Schema(description = "Nombre del estado", example = "ACTIVO")
        private String nombre;
    }
}