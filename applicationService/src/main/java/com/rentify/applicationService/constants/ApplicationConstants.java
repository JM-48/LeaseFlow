package com.rentify.applicationService.constants;

/**
 * Clase de constantes para el Application Service
 * Centraliza valores constantes utilizados en todo el microservicio
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
        throw new IllegalStateException("Clase de constantes - no debe ser instanciada");
    }

    /**
     * Estados posibles de una solicitud de arriendo
     */
    public static final class EstadoSolicitud {
        public static final String PENDIENTE = "PENDIENTE";
        public static final String ACEPTADA = "ACEPTADA";
        public static final String RECHAZADA = "RECHAZADA";

        private EstadoSolicitud() {}

        public static boolean esValido(String estado) {
            return PENDIENTE.equals(estado) ||
                    ACEPTADA.equals(estado) ||
                    RECHAZADA.equals(estado);
        }
    }

    /**
     * IDs de roles en el sistema Rentify
     * Estos corresponden a los IDs en la tabla rol de User Service
     */
    public static final class Roles {
        public static final Integer ADMIN = 1;           // rolId = 1
        public static final Integer PROPIETARIO = 2;     // rolId = 2
        public static final Integer ARRIENDATARIO = 3;   // rolId = 3

        private Roles() {}

        /**
         * Verifica si un rol puede crear solicitudes de arriendo
         * Solo ARRIENDATARIO y ADMIN pueden crear solicitudes
         */
        public static boolean puedeCrearSolicitud(Integer rolId) {
            return ARRIENDATARIO.equals(rolId) || ADMIN.equals(rolId);
        }

        /**
         * Verifica si un rol puede aceptar solicitudes
         * Solo PROPIETARIO y ADMIN pueden aceptar
         */
        public static boolean puedeAceptarSolicitud(Integer rolId) {
            return PROPIETARIO.equals(rolId) || ADMIN.equals(rolId);
        }
    }

    /**
     * Límites de negocio según reglas de Rentify
     */
    public static final class Limites {
        public static final int MAX_SOLICITUDES_ACTIVAS = 3;
        public static final int TIMEOUT_SECONDS = 5;

        private Limites() {}
    }

    /**
     * Mensajes de error comunes
     */
    public static final class Mensajes {
        public static final String USUARIO_NO_EXISTE = "El usuario con ID %d no existe";
        public static final String PROPIEDAD_NO_EXISTE = "La propiedad con ID %d no existe";
        public static final String PROPIEDAD_NO_DISPONIBLE = "La propiedad no está disponible para arriendo";
        public static final String ROL_INVALIDO_SOLICITUD = "Solo usuarios con rol ARRIENDATARIO pueden crear solicitudes de arriendo";
        public static final String MAX_SOLICITUDES_ALCANZADO = "El usuario ya tiene el máximo permitido de solicitudes activas (%d)";
        public static final String SOLICITUD_DUPLICADA = "Ya existe una solicitud pendiente para esta propiedad";
        public static final String DOCUMENTOS_NO_APROBADOS = "El usuario debe tener todos sus documentos aprobados antes de solicitar un arriendo";
        public static final String REGISTRO_SOLO_ACEPTADA = "Solo se pueden crear registros para solicitudes aceptadas. Estado actual: %s";
        public static final String REGISTRO_YA_EXISTE = "Ya existe un registro activo para esta solicitud";
        public static final String SOLICITUD_NO_ENCONTRADA = "Solicitud no encontrada con ID: %d";
        public static final String REGISTRO_NO_ENCONTRADO = "Registro no encontrado con ID: %d";
        public static final String REGISTRO_YA_INACTIVO = "El registro ya está inactivo";
        public static final String ESTADO_INVALIDO = "Estado inválido: %s";
        public static final String FECHAS_INVALIDAS = "La fecha de inicio no puede ser posterior a la fecha de fin";

        private Mensajes() {}
    }
}