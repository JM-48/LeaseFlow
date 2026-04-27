package com.rentify.documentService.constants;

/**
 * Clase de constantes para el Document Service.
 * Centraliza todos los valores constantes utilizados en el microservicio.
 */
public final class DocumentConstants {

    private DocumentConstants() {
        throw new IllegalStateException("Clase de constantes - no debe ser instanciada");
    }

    /**
     * Estados posibles de un documento.
     */
    public static final class EstadoDocumento {
        public static final String PENDIENTE = "PENDIENTE";
        public static final String ACEPTADO = "ACEPTADO";
        public static final String RECHAZADO = "RECHAZADO";
        public static final String EN_REVISION = "EN_REVISION";

        private EstadoDocumento() {}

        public static boolean esValido(String estado) {
            return PENDIENTE.equals(estado) ||
                    ACEPTADO.equals(estado) ||
                    RECHAZADO.equals(estado) ||
                    EN_REVISION.equals(estado);
        }
    }

    /**
     * Tipos de documentos válidos.
     */
    public static final class TipoDocumento {
        public static final String DNI = "DNI";
        public static final String PASAPORTE = "PASAPORTE";
        public static final String LIQUIDACION_SUELDO = "LIQUIDACION_SUELDO";
        public static final String CERTIFICADO_ANTECEDENTES = "CERTIFICADO_ANTECEDENTES";
        public static final String CERTIFICADO_AFP = "CERTIFICADO_AFP";
        public static final String CONTRATO_TRABAJO = "CONTRATO_TRABAJO";

        private TipoDocumento() {}
    }

    /**
     * Roles del sistema.
     */
    public static final class Roles {
        public static final String ADMIN = "ADMIN";
        public static final String PROPIETARIO = "PROPIETARIO";
        public static final String ARRIENDATARIO = "ARRIENDATARIO";

        private Roles() {}

        /**
         * ✅ CORREGIDO: Ahora PROPIETARIOS también pueden subir documentos
         *
         * JUSTIFICACIÓN:
         * - Todos los usuarios (PROPIETARIO y ARRIENDATARIO) necesitan verificación de identidad
         * - Los propietarios deben demostrar propiedad del inmueble
         * - El proceso de registro requiere documentos de ambos tipos de usuarios
         *
         * @param rol Nombre del rol del usuario
         * @return true si el rol puede subir documentos
         */
        public static boolean puedeSubirDocumentos(String rol) {
            return ARRIENDATARIO.equals(rol) ||
                    PROPIETARIO.equals(rol) ||    // ✅ AGREGADO: Propietarios ahora pueden subir documentos
                    ADMIN.equals(rol);
        }

        /**
         * Verifica si un rol puede validar/aprobar documentos de otros usuarios.
         *
         * @param rol Nombre del rol del usuario
         * @return true si el rol puede validar documentos
         */
        public static boolean puedeValidarDocumentos(String rol) {
            return ADMIN.equals(rol) || PROPIETARIO.equals(rol);
        }
    }

    /**
     * Límites y validaciones de negocio.
     */
    public static final class Limites {
        public static final int MAX_NOMBRE_LENGTH = 60;
        public static final int MAX_DOCUMENTOS_POR_USUARIO = 10;
        public static final int TIMEOUT_SECONDS = 5;

        private Limites() {}
    }

    /**
     * Mensajes de error estandarizados.
     */
    public static final class Mensajes {
        // Documentos
        public static final String DOCUMENTO_NO_ENCONTRADO = "El documento con ID %d no existe";
        public static final String DOCUMENTOS_NO_ENCONTRADOS_USUARIO = "No se encontraron documentos para el usuario con ID %d";

        // Estados
        public static final String ESTADO_NO_ENCONTRADO = "El estado con ID %d no existe";
        public static final String ESTADO_INVALIDO = "El estado '%s' no es válido";

        // Tipos de documento
        public static final String TIPO_DOC_NO_ENCONTRADO = "El tipo de documento con ID %d no existe";

        // Usuario
        public static final String USUARIO_NO_EXISTE = "El usuario con ID %d no existe";
        public static final String USUARIO_NO_PUEDE_SUBIR = "El usuario con rol '%s' no tiene permisos para subir documentos";

        // Validaciones
        public static final String MAX_DOCUMENTOS_ALCANZADO = "El usuario ya tiene el máximo de %d documentos permitidos";
        public static final String NOMBRE_DOCUMENTO_REQUERIDO = "El nombre del documento es obligatorio";

        // Errores de comunicación
        public static final String ERROR_COMUNICACION_USER_SERVICE = "No se pudo verificar el usuario. Intente nuevamente.";

        private Mensajes() {}
    }
}