package com.rentify.propertyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para Comunas
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Comuna dentro de una región")
public class ComunaDTO {

    @Schema(description = "ID único de la comuna",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
    @Schema(description = "Nombre de la comuna", example = "Providencia")
    private String nombre;

    @NotNull(message = "El ID de la región es obligatorio")
    @Positive(message = "El ID de la región debe ser un número positivo")
    @Schema(description = "ID de la región asociada", example = "1")
    private Long regionId;

    @Schema(description = "Información de la región",
            accessMode = Schema.AccessMode.READ_ONLY)
    private RegionDTO region;
}