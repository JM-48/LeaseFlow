package com.rentify.applicationService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de una solicitud de arriendo")
public class SolicitudArriendoDTO {

    @Schema(description = "ID único de la solicitud", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser un número positivo")
    @Schema(description = "ID del usuario que solicita el arriendo", example = "1")  // SIN required
    private Long usuarioId;

    @NotNull(message = "El ID de la propiedad es obligatorio")
    @Positive(message = "El ID de la propiedad debe ser un número positivo")
    @Schema(description = "ID de la propiedad solicitada", example = "1")  // SIN required
    private Long propiedadId;

    @Schema(description = "Estado de la solicitud (PENDIENTE, ACEPTADA, RECHAZADA)",
            example = "PENDIENTE",
            accessMode = Schema.AccessMode.READ_ONLY,
            allowableValues = {"PENDIENTE", "ACEPTADA", "RECHAZADA"})
    private String estado;

    @Schema(description = "Fecha y hora de creación de la solicitud",
            example = "2025-11-13T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Date fechaSolicitud;

    @Schema(description = "Información detallada del usuario (solo en consultas con includeDetails=true)",
            accessMode = Schema.AccessMode.READ_ONLY)
    private UsuarioDTO usuario;

    @Schema(description = "Información detallada de la propiedad (solo en consultas con includeDetails=true)",
            accessMode = Schema.AccessMode.READ_ONLY)
    private PropiedadDTO propiedad;
}