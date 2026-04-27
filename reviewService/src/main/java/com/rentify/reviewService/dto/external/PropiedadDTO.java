package com.rentify.reviewService.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO para información de propiedad desde Property Service.
 * Solo contiene los campos necesarios para el Review Service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información de la propiedad desde Property Service")
public class PropiedadDTO {

    @Schema(description = "ID de la propiedad", example = "1")
    private Long id;

    @Schema(description = "Código único de la propiedad", example = "PROP-001")
    private String codigo;

    @Schema(description = "Título de la propiedad", example = "Departamento en Providencia")
    private String titulo;

    @Schema(description = "Precio mensual", example = "650000")
    private Long precioMensual;

    @Schema(description = "Dirección de la propiedad", example = "Av. Providencia 1234")
    private String direccion;

    @Schema(description = "ID del propietario", example = "2")
    private Long propietarioId;

    @Schema(description = "Estado de la propiedad", example = "DISPONIBLE")
    private String estado;
}