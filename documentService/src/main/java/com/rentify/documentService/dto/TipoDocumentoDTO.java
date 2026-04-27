package com.rentify.documentService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para tipos de documentos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tipo de documento")
public class TipoDocumentoDTO {

    @Schema(description = "ID único del tipo de documento",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre del tipo de documento es obligatorio")
    @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
    @Schema(description = "Nombre del tipo de documento",
            example = "LIQUIDACION_SUELDO",
            allowableValues = {"DNI", "PASAPORTE", "LIQUIDACION_SUELDO",
                    "CERTIFICADO_ANTECEDENTES", "CERTIFICADO_AFP",
                    "CONTRATO_TRABAJO"})
    private String nombre;
}