package com.rentify.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Entidad Rol - Representa los roles del sistema
 * Roles: ADMIN (1), PROPIETARIO (2), ARRIENDATARIO (3)
 */
@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Column(nullable = false, length = 60, unique = true)
    private String nombre;
}