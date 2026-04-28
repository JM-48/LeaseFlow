package com.rentify.contactService.dto;

import com.rentify.contactService.dto.external.UsuarioDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un mensaje de contacto")
public class MensajeContactoDTO {

    @Schema(description = "ID único del mensaje",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre completo de quien envía el mensaje",
            example = "Juan Pérez")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Schema(description = "Email de contacto",
            example = "juan.perez@email.com")
    private String email;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 200, message = "El asunto no puede exceder 200 caracteres")
    @Schema(description = "Asunto del mensaje",
            example = "Consulta sobre arriendo de departamento")
    private String asunto;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 10, max = 5000, message = "El mensaje debe tener entre 10 y 5000 caracteres")
    @Schema(description = "Contenido del mensaje",
            example = "Quisiera obtener más información sobre el departamento en Providencia...")
    private String mensaje;

    @Size(max = 20, message = "El número de teléfono no puede exceder 20 caracteres")
    @Schema(description = "Número de teléfono opcional",
            example = "+56912345678")
    private String numeroTelefono;

    @Schema(description = "ID del usuario autenticado (opcional)",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long usuarioId;

    @Schema(description = "Estado del mensaje",
            example = "PENDIENTE",
            accessMode = Schema.AccessMode.READ_ONLY,
            allowableValues = {"PENDIENTE", "EN_PROCESO", "RESUELTO"})
    private String estado;

    @Schema(description = "Fecha y hora de creación del mensaje",
            example = "2025-11-20T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Date fechaCreacion;

    @Schema(description = "Fecha y hora de última actualización",
            example = "2025-11-20T15:45:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Date fechaActualizacion;

    @Schema(description = "Respuesta del administrador",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String respuesta;

    @Schema(description = "ID del administrador que respondió",
            example = "5",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long respondidoPor;

    // Campo opcional con información del usuario (solo cuando includeDetails=true)
    @Schema(description = "Información detallada del usuario",
            accessMode = Schema.AccessMode.READ_ONLY)
    private UsuarioDTO usuario;
}