package com.rentify.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO para registro y consulta de usuarios
 * MODIFICADO: snombre ahora es OPCIONAL
 * Las fechas se manejan como String en formato "yyyy-MM-dd"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos del usuario")
public class UsuarioDTO {

    @Schema(description = "ID único del usuario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El primer nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El primer nombre debe tener entre 2 y 50 caracteres")
    @Schema(description = "Primer nombre del usuario", example = "Juan")
    private String pnombre;

    // MODIFICADO: Segundo nombre ahora es OPCIONAL (sin @NotBlank)
    @Size(max = 50, message = "El segundo nombre no puede exceder 50 caracteres")
    @Schema(description = "Segundo nombre del usuario (opcional)", example = "Carlos")
    private String snombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Schema(description = "Apellido paterno del usuario", example = "Pérez")
    private String papellido;

    @NotBlank(message = "La fecha de nacimiento es obligatoria")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Formato de fecha inválido. Use yyyy-MM-dd")
    @Schema(description = "Fecha de nacimiento en formato yyyy-MM-dd", example = "1995-05-15")
    private String fnacimiento;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email es inválido")
    @Schema(description = "Correo electrónico", example = "juan.perez@email.com")
    private String email;

    @NotBlank(message = "El RUT es obligatorio")
    @Schema(description = "RUT del usuario", example = "12345678-9")
    private String rut;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 8, max = 15, message = "El teléfono debe tener entre 8 y 15 caracteres")
    @Schema(description = "Número de teléfono", example = "+56912345678")
    private String ntelefono;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Schema(description = "Contraseña del usuario")
    private String clave;

    @Schema(description = "Indica si es usuario VIP de DUOC", example = "false", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean duocVip;

    @Schema(description = "Puntos RentifyPoints acumulados", example = "0", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer puntos;

    @Schema(description = "Código de referido único", example = "ABC123XY", accessMode = Schema.AccessMode.READ_ONLY)
    private String codigoRef;

    @Schema(description = "Fecha de creación en formato yyyy-MM-dd", accessMode = Schema.AccessMode.READ_ONLY)
    private String fcreacion;

    @Schema(description = "Fecha de última actualización en formato yyyy-MM-dd", accessMode = Schema.AccessMode.READ_ONLY)
    private String factualizacion;

    @Schema(description = "ID del estado del usuario", example = "1")
    private Long estadoId;

    @Schema(description = "ID del rol del usuario", example = "3")
    private Long rolId;

    // Campos expandidos (cuando includeDetails=true)
    @Schema(description = "Información del rol", accessMode = Schema.AccessMode.READ_ONLY)
    private RolDTO rol;

    @Schema(description = "Información del estado", accessMode = Schema.AccessMode.READ_ONLY)
    private EstadoDTO estado;
}