package com.rentify.contactService.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO del usuario desde User Service
 * IMPORTANTE: Debe coincidir con la estructura que devuelve User Service
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información del usuario desde User Service")
public class UsuarioDTO {
    private Long id;
    private String pnombre;
    private String snombre;
    private String papellido;
    private String email;

    // CAMBIO CRÍTICO: Rol y Estado son objetos, no strings
    private RolDTO rol;
    private EstadoDTO estado;

    // Nested DTOs
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolDTO {
        private Long id;
        private String nombre;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadoDTO {
        private Long id;
        private String nombre;
    }

    // Métodos helper para compatibilidad con código existente
    public String getRolNombre() {
        return rol != null ? rol.getNombre() : null;
    }

    public String getEstadoNombre() {
        return estado != null ? estado.getNombre() : null;
    }
}