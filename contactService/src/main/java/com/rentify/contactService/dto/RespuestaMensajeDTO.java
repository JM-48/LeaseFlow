package com.rentify.contactService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para responder un mensaje de contacto")
public class RespuestaMensajeDTO {

    @NotBlank(message = "La respuesta es obligatoria")
    @Size(min = 10, max = 5000, message = "La respuesta debe tener entre 10 y 5000 caracteres")
    @Schema(description = "Contenido de la respuesta",
            example = "Hola Juan, gracias por contactarnos. Con respecto a tu consulta...")
    private String respuesta;

    @NotNull(message = "El ID del administrador es obligatorio")
    @Schema(description = "ID del administrador que responde",
            example = "5")
    private Long respondidoPor;

    @Schema(description = "Nuevo estado del mensaje (opcional, por defecto se marca como RESUELTO)",
            example = "RESUELTO",
            allowableValues = {"PENDIENTE", "EN_PROCESO", "RESUELTO"})
    private String nuevoEstado;
}