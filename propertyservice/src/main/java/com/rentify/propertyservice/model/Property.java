package com.rentify.propertyservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una Propiedad en el sistema.
 */
@Entity
@Table(name = "propiedad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Column(name = "codigo", length = 10, nullable = false, unique = true)
    private String codigo;

    @NotBlank(message = "El título es obligatorio")
    @Column(name = "titulo", length = 100, nullable = false)
    private String titulo;

    @NotNull(message = "El precio mensual es obligatorio")
    @Column(name = "precio_mensual", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioMensual;

    @NotBlank(message = "La divisa es obligatoria")
    @Column(name = "divisa", length = 20, nullable = false)
    private String divisa;

    @NotNull(message = "Los metros cuadrados son obligatorios")
    @Column(name = "m2", precision = 12, scale = 2, nullable = false)
    private BigDecimal m2;

    @NotNull(message = "El número de habitaciones es obligatorio")
    @Column(name = "n_habit")
    private Integer nHabit;

    @NotNull(message = "El número de baños es obligatorio")
    @Column(name = "n_banos")
    private Integer nBanos;

    @Column(name = "pet_friendly")
    private Boolean petFriendly;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(name = "direccion", length = 200, nullable = false)
    private String direccion;

    @Column(name = "fcreacion")
    private LocalDate fcreacion;

    @NotNull(message = "El tipo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", nullable = false)
    private Tipo tipo;

    @NotNull(message = "La comuna es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comuna_id", nullable = false)
    private Comuna comuna;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Foto> fotos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "mas_atributos",
            joinColumns = @JoinColumn(name = "propiedad_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    @Builder.Default
    private List<Categoria> categorias = new ArrayList<>();

    /**
     * Inicializa valores por defecto antes de persistir.
     */
    @PrePersist
    protected void onCreate() {
        if (fcreacion == null) {
            fcreacion = LocalDate.now();
        }
        if (petFriendly == null) {
            petFriendly = false;
        }
        if (divisa == null) {
            divisa = "CLP";
        }
    }

    /**
     * Método helper para agregar una foto.
     */
    public void addFoto(Foto foto) {
        fotos.add(foto);
        foto.setProperty(this);
    }

    /**
     * Método helper para remover una foto.
     */
    public void removeFoto(Foto foto) {
        fotos.remove(foto);
        foto.setProperty(null);
    }

    /**
     * Método helper para agregar una categoría.
     */
    public void addCategoria(Categoria categoria) {
        categorias.add(categoria);
    }

    /**
     * Método helper para remover una categoría.
     */
    public void removeCategoria(Categoria categoria) {
        categorias.remove(categoria);
    }
}