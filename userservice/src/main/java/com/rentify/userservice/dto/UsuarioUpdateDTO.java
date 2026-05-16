package com.rentify.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO para actualizacion parcial de usuario
 * Solo contiene los campos que pueden ser actualizados por el admin
 * No requiere campos sensibles como clave, rut, fnacimiento
 * MODIFICADO: snombre ahora es OPCIONAL
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para actualizar un usuario existente")
public class UsuarioUpdateDTO {

    @NotBlank(message = "El primer nombre es obligatorio")
    @Size(max = 60, message = "El primer nombre no puede exceder 60 caracteres")
    @Schema(description = "Primer nombre del usuario", example = "Juan")
    private String pnombre;

    // MODIFICADO: Segundo nombre ahora es OPCIONAL (sin @NotBlank)
    @Size(max = 60, message = "El segundo nombre no puede exceder 60 caracteres")
    @Schema(description = "Segundo nombre del usuario (opcional)", example = "Carlos")
    private String snombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 60, message = "El apellido no puede exceder 60 caracteres")
    @Schema(description = "Apellido paterno", example = "Perez")
    private String papellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es valido")
    @Size(max = 200)
    @Schema(description = "Correo electronico", example = "juan.perez@email.com")
    private String email;

    @NotBlank(message = "El telefono es obligatorio")
    @Size(min = 9, max = 12, message = "El telefono debe tener entre 9 y 12 caracteres")
    @Schema(description = "Numero de telefono", example = "987654321")
    private String ntelefono;

    @Schema(description = "ID del rol asignado (1=ADMIN, 2=PROPIETARIO, 3=ARRIENDATARIO)",
            example = "3")
    private Long rolId;

    @Schema(description = "ID del estado (1=ACTIVO, 2=INACTIVO, 3=SUSPENDIDO)",
            example = "1")
    private Long estadoId;
}