package com.rentify.documentService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Entidad que representa un estado de documento.
 * Ejemplos: PENDIENTE, ACEPTADO, RECHAZADO, EN_REVISION
 */
@Entity
@Table(name = "estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nombre del estado es obligatorio")
    @Column(name = "nombre", nullable = false, length = 20)
    private String nombre;
}