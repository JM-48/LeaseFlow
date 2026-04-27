package com.rentify.propertyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para Categorías de propiedades
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Categoría de propiedad (Amoblado, Pet-Friendly, etc.)")
public class CategoriaDTO {

    @Schema(description = "ID único de la categoría",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
    @Schema(description = "Nombre de la categoría", example = "Departamentos Amoblados")
    private String nombre;
}