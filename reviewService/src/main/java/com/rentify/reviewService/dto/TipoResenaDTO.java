package com.rentify.reviewService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para transferencia de datos de tipos de reseña.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un tipo de reseña")
public class TipoResenaDTO {

    @Schema(description = "ID único del tipo de reseña",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
    @Schema(description = "Nombre del tipo de reseña",
            example = "RESENA_PROPIEDAD")
    private String nombre;
}