package com.rentify.documentService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para estados de documentos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Estado de un documento")
public class EstadoDTO {

    @Schema(description = "ID único del estado",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre del estado es obligatorio")
    @Size(max = 20, message = "El nombre no puede exceder 20 caracteres")
    @Schema(description = "Nombre del estado",
            example = "PENDIENTE",
            allowableValues = {"PENDIENTE", "ACEPTADO", "RECHAZADO", "EN_REVISION"})
    private String nombre;
}