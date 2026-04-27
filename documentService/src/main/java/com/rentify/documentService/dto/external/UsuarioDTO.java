package com.rentify.documentService.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO externo para información de usuarios desde User Service.
 * Solo contiene campos necesarios para Document Service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información del usuario desde User Service")
public class UsuarioDTO {

    @Schema(description = "ID único del usuario", example = "1")
    private Long id;

    @Schema(description = "Primer nombre del usuario", example = "Juan")
    private String pnombre;

    @Schema(description = "Primer apellido del usuario", example = "Pérez")
    private String papellido;

    @Schema(description = "Email del usuario", example = "juan.perez@email.com")
    private String email;

    // CAMBIO AQUÍ: De String a RolDTO
    @Schema(description = "Rol del usuario en el sistema")
    private RolDTO rol;

    // CAMBIO AQUÍ: De String a EstadoDTO
    @Schema(description = "Estado del usuario")
    private EstadoDTO estado;

    // Clases internas para mapear los objetos anidados
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
}