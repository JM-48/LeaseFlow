package com.rentify.documentService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request para actualizar estado de documento con observaciones.
 * Usado principalmente para rechazos donde se requiere un motivo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para actualizar estado de documento")
public class ActualizarEstadoRequest {

    @NotNull(message = "El ID del estado es requerido")
    @Schema(description = "ID del nuevo estado",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long estadoId;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Schema(description = "Observaciones o motivo (obligatorio para rechazos)",
            example = "Documento ilegible, imagen borrosa")
    private String observaciones;

    @Schema(description = "ID del administrador que realiza la accion",
            example = "1")
    private Long revisadoPor;
}