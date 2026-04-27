package com.rentify.propertyservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para operaciones con Propiedades
 *
 * SOLUCIONES APLICADAS:
 * ✅ int primitivo para nHabit y nBanos (evita problemas de deserialización con 0)
 * ✅ @JsonProperty + @JsonAlias para serialización/deserialización correcta
 * ✅ @JsonFormat para LocalDate (serializa como "yyyy-MM-dd" en lugar de array)
 * ✅ Getters/Setters personalizados para evitar duplicados de Jackson
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de una propiedad")
public class PropertyDTO {

    @Schema(description = "ID único de la propiedad",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 10, message = "El código no puede exceder 10 caracteres")
    @Schema(description = "Código único de la propiedad", example = "DP001")
    private String codigo;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede exceder 100 caracteres")
    @Schema(description = "Título descriptivo de la propiedad",
            example = "Departamento 2D/2B en Providencia")
    private String titulo;

    @NotNull(message = "El precio mensual es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Schema(description = "Precio mensual de arriendo", example = "650000")
    private BigDecimal precioMensual;

    @NotBlank(message = "La divisa es obligatoria")
    @Pattern(regexp = "CLP|USD|EUR", message = "La divisa debe ser CLP, USD o EUR")
    @Schema(description = "Divisa del precio", example = "CLP", allowableValues = {"CLP", "USD", "EUR"})
    private String divisa;

    @NotNull(message = "Los metros cuadrados son obligatorios")
    @DecimalMin(value = "1.0", message = "Los m² deben ser al menos 1")
    @DecimalMax(value = "10000.0", message = "Los m² no pueden exceder 10000")
    @Schema(description = "Metros cuadrados de la propiedad", example = "65.5")
    private BigDecimal m2;

    @NotNull(message = "El número de habitaciones es obligatorio")
    @Min(value = 0, message = "El número de habitaciones no puede ser negativo")
    @Max(value = 50, message = "El número de habitaciones no puede exceder 50")
    @JsonProperty("nHabit")
    @JsonAlias({"nhabit"})
    @Schema(description = "Número de habitaciones", example = "2")
    private int nHabit;

    @NotNull(message = "El número de baños es obligatorio")
    @Min(value = 0, message = "El número de baños no puede ser negativo")
    @Max(value = 20, message = "El número de baños no puede exceder 20")
    @JsonProperty("nBanos")
    @JsonAlias({"nbanos"})
    @Schema(description = "Número de baños", example = "2")
    private int nBanos;

    @Schema(description = "Indica si acepta mascotas", example = "true")
    private Boolean petFriendly;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    @Schema(description = "Dirección completa de la propiedad",
            example = "Av. Providencia 1234, Depto 501")
    private String direccion;

    @Schema(description = "Fecha de creación de la publicación",
            example = "2025-01-15",
            accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fcreacion;

    @NotNull(message = "El tipo de propiedad es obligatorio")
    @Positive(message = "El ID del tipo debe ser un número positivo")
    @Schema(description = "ID del tipo de propiedad", example = "1")
    private Long tipoId;

    @NotNull(message = "La comuna es obligatoria")
    @Positive(message = "El ID de la comuna debe ser un número positivo")
    @Schema(description = "ID de la comuna", example = "2")
    private Long comunaId;

    // Campos de solo lectura (relaciones)
    @Schema(description = "Información del tipo de propiedad",
            accessMode = Schema.AccessMode.READ_ONLY)
    private TipoDTO tipo;

    @Schema(description = "Información de la comuna",
            accessMode = Schema.AccessMode.READ_ONLY)
    private ComunaDTO comuna;

    @Schema(description = "Lista de fotos de la propiedad",
            accessMode = Schema.AccessMode.READ_ONLY)
    private List<FotoDTO> fotos;

    @Schema(description = "Lista de categorías asociadas",
            accessMode = Schema.AccessMode.READ_ONLY)
    private List<CategoriaDTO> categorias;

    // ✅ GETTERS/SETTERS PERSONALIZADOS para nHabit
    @JsonIgnore
    public int getNHabit() {
        return nHabit;
    }

    @JsonIgnore
    public void setNHabit(int nHabit) {
        this.nHabit = nHabit;
    }

    // ✅ GETTERS/SETTERS PERSONALIZADOS para nBanos
    @JsonIgnore
    public int getNBanos() {
        return nBanos;
    }

    @JsonIgnore
    public void setNBanos(int nBanos) {
        this.nBanos = nBanos;
    }
}