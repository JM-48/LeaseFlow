package com.rentify.reviewService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Entidad que representa un tipo de reseña en el sistema.
 * Ejemplos: "Reseña de Propiedad", "Reseña de Usuario", etc.
 */
@Entity
@Table(name = "tipo_resena")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoResena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del tipo de reseña es obligatorio")
    @Column(nullable = false, length = 60, unique = true)
    private String nombre;
}