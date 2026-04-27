package com.rentify.documentService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

/**
 * Entidad que representa un documento subido por un usuario.
 * Almacena referencias por ID a usuario, estado y tipo de documento.
 */
@Entity
@Table(name = "documentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha de subida es obligatoria")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "f_subido", nullable = false)
    private Date fechaSubido;

    @NotNull(message = "El nombre del documento es obligatorio")
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Column(name = "usuarios_id", nullable = false)
    private Long usuarioId;

    @NotNull(message = "El ID del estado es obligatorio")
    @Column(name = "estado_id", nullable = false)
    private Long estadoId;

    @NotNull(message = "El ID del tipo de documento es obligatorio")
    @Column(name = "tipo_doc_id", nullable = false)
    private Long tipoDocId;

    // ==================== NUEVOS CAMPOS ====================

    /**
     * Observaciones o motivo de rechazo.
     * El admin escribe aqui el motivo cuando rechaza un documento.
     */
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    /**
     * Fecha de ultima actualizacion del estado.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "f_actualizacion")
    private Date fechaActualizacion;

    /**
     * ID del administrador que reviso/actualizo el documento.
     */
    @Column(name = "revisado_por")
    private Long revisadoPor;

    // ==================== CALLBACKS ====================

    /**
     * Callback que se ejecuta antes de persistir la entidad.
     * Establece valores por defecto si no estan definidos.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaSubido == null) {
            fechaSubido = new Date();
        }
        fechaActualizacion = new Date();
    }

    /**
     * Callback que se ejecuta antes de actualizar la entidad.
     */
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = new Date();
    }
}