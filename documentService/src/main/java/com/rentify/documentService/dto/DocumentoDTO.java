package com.rentify.documentService.dto;

import com.rentify.documentService.dto.external.UsuarioDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * DTO para transferencia de datos de documentos.
 * Usado tanto para request como response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Datos de un documento de usuario")
public class DocumentoDTO {

    @Schema(description = "ID unico del documento",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre del documento es obligatorio")
    @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
    @Schema(description = "Nombre descriptivo del documento",
            example = "Liquidacion_Sueldo_Enero_2025.pdf")
    private String nombre;

    @Schema(description = "Fecha y hora de subida del documento",
            example = "2025-11-18T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Date fechaSubido;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser un numero positivo")
    @Schema(description = "ID del usuario propietario del documento",
            example = "1")
    private Long usuarioId;

    @NotNull(message = "El ID del estado es obligatorio")
    @Positive(message = "El ID del estado debe ser un numero positivo")
    @Schema(description = "ID del estado del documento",
            example = "1")
    private Long estadoId;

    @NotNull(message = "El ID del tipo de documento es obligatorio")
    @Positive(message = "El ID del tipo de documento debe ser un numero positivo")
    @Schema(description = "ID del tipo de documento",
            example = "1")
    private Long tipoDocId;

    // ==================== NUEVOS CAMPOS ====================

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Schema(description = "Observaciones o motivo de rechazo del documento",
            example = "Documento ilegible, por favor suba una imagen mas clara")
    private String observaciones;

    @Schema(description = "Fecha de ultima actualizacion del estado",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Date fechaActualizacion;

    @Schema(description = "ID del administrador que reviso el documento",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long revisadoPor;

    // ==================== CAMPOS EXPANDIDOS ====================

    @Schema(description = "Nombre del estado del documento",
            example = "PENDIENTE",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String estadoNombre;

    @Schema(description = "Nombre del tipo de documento",
            example = "LIQUIDACION_SUELDO",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String tipoDocNombre;

    @Schema(description = "Informacion detallada del usuario propietario",
            accessMode = Schema.AccessMode.READ_ONLY)
    private UsuarioDTO usuario;
}