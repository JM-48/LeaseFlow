package com.rentify.contactService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "mensajes_contacto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Column(nullable = false, length = 200)
    private String email;

    @NotBlank(message = "El asunto es obligatorio")
    @Column(nullable = false, length = 200)
    private String asunto;

    @NotBlank(message = "El mensaje es obligatorio")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "numero_telefono", length = 20)
    private String numeroTelefono;

    @Column(name = "usuario_id")
    private Long usuarioId; // Opcional: si el usuario está autenticado

    @NotNull(message = "El estado es obligatorio")
    @Column(nullable = false, length = 20)
    private String estado; // PENDIENTE, EN_PROCESO, RESUELTO

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_creacion", nullable = false)
    private Date fechaCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_actualizacion")
    private Date fechaActualizacion;

    @Column(name = "respuesta", columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "respondido_por")
    private Long respondidoPor; // ID del admin que respondió

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = new Date();
    }
}