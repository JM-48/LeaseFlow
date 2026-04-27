package com.rentify.reviewService.dto;

import com.rentify.reviewService.dto.external.PropiedadDTO;
import com.rentify.reviewService.dto.external.UsuarioDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

/**
 * DTO para transferencia de datos de reseñas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de una reseña o valoración")
public class ReviewDTO {

    @Schema(description = "ID único de la reseña",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser un número positivo")
    @Schema(description = "ID del usuario que crea la reseña", example = "1")
    private Long usuarioId;

    @Positive(message = "El ID de la propiedad debe ser un número positivo")
    @Schema(description = "ID de la propiedad reseñada (si aplica)", example = "1")
    private Long propiedadId;

    @Positive(message = "El ID del usuario reseñado debe ser un número positivo")
    @Schema(description = "ID del usuario reseñado (si aplica)", example = "2")
    private Long usuarioResenadoId;

    @NotNull(message = "El puntaje es obligatorio")
    @Min(value = 1, message = "El puntaje mínimo es 1")
    @Max(value = 10, message = "El puntaje máximo es 10")
    @Schema(description = "Puntaje de la reseña", example = "8", minimum = "1", maximum = "10")
    private Integer puntaje;

    @Size(min = 10, max = 500, message = "El comentario debe tener entre 10 y 500 caracteres")
    @Schema(description = "Comentario de la reseña",
            example = "Excelente propiedad, muy bien ubicada y en perfectas condiciones.")
    private String comentario;

    @NotNull(message = "El tipo de reseña es obligatorio")
    @Positive(message = "El ID del tipo de reseña debe ser un número positivo")
    @Schema(description = "ID del tipo de reseña", example = "1")
    private Long tipoResenaId;

    @Schema(description = "Fecha y hora de creación de la reseña",
            example = "2025-11-19T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Date fechaResena;

    @Schema(description = "Fecha y hora de baneo (si fue baneada)",
            example = "2025-11-20T15:45:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Date fechaBaneo;

    @Schema(description = "Estado de la reseña",
            example = "ACTIVA",
            accessMode = Schema.AccessMode.READ_ONLY,
            allowableValues = {"ACTIVA", "BANEADA", "OCULTA"})
    private String estado;

    // Relaciones opcionales (solo en consultas con includeDetails=true)
    @Schema(description = "Información detallada del usuario que creó la reseña",
            accessMode = Schema.AccessMode.READ_ONLY)
    private UsuarioDTO usuario;

    @Schema(description = "Información detallada de la propiedad reseñada",
            accessMode = Schema.AccessMode.READ_ONLY)
    private PropiedadDTO propiedad;

    @Schema(description = "Información detallada del usuario reseñado",
            accessMode = Schema.AccessMode.READ_ONLY)
    private UsuarioDTO usuarioResenado;

    @Schema(description = "Nombre del tipo de reseña",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String tipoResenaNombre;
}