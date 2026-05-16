package com.rentify.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad Usuario - Representa un usuario del sistema Rentify
 * Puede ser ADMIN, PROPIETARIO o ARRIENDATARIO
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El primer nombre es obligatorio")
    @Column(name = "pnombre", nullable = false, length = 60)
    private String pnombre;

    @NotBlank(message = "El segundo nombre es obligatorio")
    @Column(name = "snombre", nullable = false, length = 60)
    private String snombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Column(name = "papellido", nullable = false, length = 60)
    private String papellido;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @Column(name = "fnacimiento", nullable = false)
    private LocalDate fnacimiento;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Column(nullable = false, length = 200, unique = true)
    private String email;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "Formato de RUT inválido")
    @Column(nullable = false, length = 14, unique = true)
    private String rut;

    @NotBlank(message = "El número de teléfono es obligatorio")
    @Column(name = "ntelefono", nullable = false, length = 12)
    private String ntelefono;

    @Builder.Default
    @Column(name = "duoc_vip", nullable = false)
    private Boolean duocVip = false;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Column(nullable = false, length = 100)
    private String clave;

    @Builder.Default
    @Column(nullable = false)
    private Integer puntos = 0;

    @Column(name = "codigo_ref", nullable = false, length = 20, unique = true)
    private String codigoRef;

    @Column(name = "fcreacion", nullable = false)
    private LocalDate fcreacion;

    @Column(name = "factualizacion", nullable = false)
    private LocalDate factualizacion;

    @Column(name = "estado_id", nullable = false)
    private Long estadoId;

    @Column(name = "rol_id")
    private Long rolId;

    /**
     * Se ejecuta antes de persistir la entidad
     * Establece valores por defecto
     */
    @PrePersist
    protected void onCreate() {
        if (fcreacion == null) {
            fcreacion = LocalDate.now();
        }
        if (factualizacion == null) {
            factualizacion = LocalDate.now();
        }
        if (puntos == null) {
            puntos = 0;
        }
        if (duocVip == null) {
            duocVip = false;
        }
    }

    /**
     * Se ejecuta antes de actualizar la entidad
     */
    @PreUpdate
    protected void onUpdate() {
        factualizacion = LocalDate.now();
    }
}