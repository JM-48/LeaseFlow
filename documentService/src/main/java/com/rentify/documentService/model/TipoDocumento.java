package com.rentify.documentService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Entidad que representa un tipo de documento.
 * Ejemplos: DNI, PASAPORTE, LIQUIDACION_SUELDO, CERTIFICADO_ANTECEDENTES
 */
@Entity
@Table(name = "tipo_doc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nombre del tipo de documento es obligatorio")
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;
}