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
@Schema(description = "Datos de un rol del sistema")
public class RolDTO {

    @Schema(description = "ID único del rol",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
    @Schema(description = "Nombre del rol",
            example = "ARRIENDATARIO",
            allowableValues = {"ADMIN", "PROPIETARIO", "ARRIENDATARIO"})
    private String nombre;
}