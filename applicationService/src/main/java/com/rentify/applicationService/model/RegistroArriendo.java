package com.rentify.applicationService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "registros_arriendo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroArriendo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID de la solicitud es obligatorio")
    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_inicio", nullable = false)
    private Date fechaInicio;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_fin")
    private Date fechaFin;

    @NotNull(message = "El monto mensual es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    @Column(name = "monto_mensual", nullable = false)
    private Double montoMensual;

    @NotNull(message = "El estado activo es obligatorio")
    @Column(nullable = false)
    private Boolean activo;

    @PrePersist
    protected void onCreate() {
        if (activo == null) {
            activo = Boolean.TRUE;  // CORREGIDO: usar Boolean.TRUE en lugar de true
        }
    }
}