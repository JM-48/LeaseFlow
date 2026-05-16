package com.rentify.userservice.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad Estado - Representa los estados posibles de un usuario
 * Estados: ACTIVO (1), INACTIVO (2), SUSPENDIDO (3)
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

    @Column(nullable = false, length = 20, unique = true)
    private String nombre;
}