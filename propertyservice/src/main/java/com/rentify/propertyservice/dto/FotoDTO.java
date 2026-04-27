package com.rentify.propertyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para Fotos de propiedades
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de una foto de propiedad")
public class FotoDTO {

    @Schema(description = "ID único de la foto",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
    @Schema(description = "Nombre del archivo", example = "sala_estar.jpg")
    private String nombre;

    @NotBlank(message = "La URL es obligatoria")
    @Schema(description = "URL o ruta de la foto",
            example = "uploads/properties/1/1234567890_sala_estar.jpg")
    private String url;

    @Schema(description = "Orden de visualización de la foto", example = "1")
    private Integer sortOrder;

    @NotNull(message = "El ID de la propiedad es obligatorio")
    @Positive(message = "El ID de la propiedad debe ser un número positivo")
    @Schema(description = "ID de la propiedad asociada", example = "1")
    private Long propiedadId;
}