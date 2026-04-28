package com.rentify.applicationService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudArriendo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @NotNull(message = "El ID de la propiedad es obligatorio")
    @Column(name = "propiedad_id", nullable = false)
    private Long propiedadId;

    @NotNull(message = "El estado es obligatorio")
    @Column(nullable = false, length = 20)
    private String estado; // PENDIENTE, ACEPTADA, RECHAZADA

    @NotNull(message = "La fecha de solicitud es obligatoria")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_solicitud", nullable = false)
    private Date fechaSolicitud;

    @PrePersist
    protected void onCreate() {
        if (fechaSolicitud == null) {
            fechaSolicitud = new Date();
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}