package com.rentify.reviewService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

/**
 * Entidad que representa una reseña/valoración en el sistema.
 * Puede ser una reseña de una propiedad o de un usuario.
 */
@Entity
@Table(name = "resenas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "propiedad_id")
    private Long propiedadId;

    @Column(name = "usuario_resenado_id")
    private Long usuarioResenadoId;

    @NotNull(message = "El puntaje es obligatorio")
    @Min(value = 1, message = "El puntaje mínimo es 1")
    @Max(value = 10, message = "El puntaje máximo es 10")
    @Column(nullable = false)
    private Integer puntaje;

    @Column(length = 500)
    private String comentario;

    @NotNull(message = "El tipo de reseña es obligatorio")
    @Column(name = "tipo_resena_id", nullable = false)
    private Long tipoResenaId;

    @NotNull(message = "La fecha de creación es obligatoria")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_resena", nullable = false)
    private Date fechaResena;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_baneo")
    private Date fechaBaneo;

    @Column(length = 20)
    private String estado;

    /**
     * Método ejecutado antes de persistir la entidad.
     * Establece valores por defecto.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaResena == null) {
            fechaResena = new Date();
        }
        if (estado == null) {
            estado = "ACTIVA";
        }
    }
}