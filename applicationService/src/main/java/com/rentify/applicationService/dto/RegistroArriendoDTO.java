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
@Schema(description = "Datos de un registro de arriendo activo")
public class RegistroArriendoDTO {

    @Schema(description = "ID único del registro", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "El ID de la solicitud es obligatorio")
    @Positive(message = "El ID de la solicitud debe ser un número positivo")
    @Schema(description = "ID de la solicitud asociada", example = "1")  // SIN required
    private Long solicitudId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Schema(description = "Fecha de inicio del arriendo",
            example = "2025-12-01",
            type = "string",
            format = "date")  // SIN required
    private Date fechaInicio;

    @Schema(description = "Fecha de finalización del arriendo (opcional al crear)",
            example = "2026-12-01",
            type = "string",
            format = "date")
    private Date fechaFin;

    @NotNull(message = "El monto mensual es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    @Schema(description = "Monto mensual del arriendo en CLP",
            example = "500000.00")  // SIN required
    private Double montoMensual;

    @Schema(description = "Indica si el registro está activo",
            example = "true",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean activo;

    @Schema(description = "Información detallada de la solicitud (solo en consultas con includeDetails=true)",
            accessMode = Schema.AccessMode.READ_ONLY)
    private SolicitudArriendoDTO solicitud;
}