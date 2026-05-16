//package com.rentify.documentservice.model;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
///**
// * Entidad Documento para DocumentService
// * Incluye campo observaciones para motivos de rechazo
// */
//@Entity
//@Table(name = "documentos")
//public class Documento {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String nombre;
//
//    @Column(name = "fecha_subido")
//    private LocalDateTime fechaSubido;
//
//    @Column(name = "usuario_id", nullable = false)
//    private Long usuarioId;
//
//    @Column(name = "estado_id", nullable = false)
//    private Long estadoId;
//
//    @Column(name = "tipo_doc_id", nullable = false)
//    private Long tipoDocId;
//
//    // Campo para motivo de rechazo u observaciones del admin
//    @Column(length = 500)
//    private String observaciones;
//
//    // Fecha de ultima actualizacion de estado
//    @Column(name = "fecha_actualizacion")
//    private LocalDateTime fechaActualizacion;
//
//    // ID del admin que reviso el documento
//    @Column(name = "revisado_por")
//    private Long revisadoPor;
//
//    @PrePersist
//    protected void onCreate() {
//        fechaSubido = LocalDateTime.now();
//        fechaActualizacion = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        fechaActualizacion = LocalDateTime.now();
//    }
//
//    // Constructors
//    public Documento() {}
//
//    public Documento(String nombre, Long usuarioId, Long estadoId, Long tipoDocId) {
//        this.nombre = nombre;
//        this.usuarioId = usuarioId;
//        this.estadoId = estadoId;
//        this.tipoDocId = tipoDocId;
//    }
//
//    // Getters and Setters
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getNombre() {
//        return nombre;
//    }
//
//    public void setNombre(String nombre) {
//        this.nombre = nombre;
//    }
//
//    public LocalDateTime getFechaSubido() {
//        return fechaSubido;
//    }
//
//    public void setFechaSubido(LocalDateTime fechaSubido) {
//        this.fechaSubido = fechaSubido;
//    }
//
//    public Long getUsuarioId() {
//        return usuarioId;
//    }
//
//    public void setUsuarioId(Long usuarioId) {
//        this.usuarioId = usuarioId;
//    }
//
//    public Long getEstadoId() {
//        return estadoId;
//    }
//
//    public void setEstadoId(Long estadoId) {
//        this.estadoId = estadoId;
//    }
//
//    public Long getTipoDocId() {
//        return tipoDocId;
//    }
//
//    public void setTipoDocId(Long tipoDocId) {
//        this.tipoDocId = tipoDocId;
//    }
//
//    public String getObservaciones() {
//        return observaciones;
//    }
//
//    public void setObservaciones(String observaciones) {
//        this.observaciones = observaciones;
//    }
//
//    public LocalDateTime getFechaActualizacion() {
//        return fechaActualizacion;
//    }
//
//    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
//        this.fechaActualizacion = fechaActualizacion;
//    }
//
//    public Long getRevisadoPor() {
//        return revisadoPor;
//    }
//
//    public void setRevisadoPor(Long revisadoPor) {
//        this.revisadoPor = revisadoPor;
//    }
//}