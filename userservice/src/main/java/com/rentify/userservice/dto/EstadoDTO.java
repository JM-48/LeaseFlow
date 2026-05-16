package com.rentify.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un estado del sistema")
public class EstadoDTO {

    @Schema(description = "ID único del estado",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre del estado es obligatorio")
    @Size(max = 20, message = "El nombre no puede exceder 20 caracteres")
    @Schema(description = "Nombre del estado",
            example = "ACTIVO",
            allowableValues = {"ACTIVO", "INACTIVO", "SUSPENDIDO"})
    private String nombre;
}