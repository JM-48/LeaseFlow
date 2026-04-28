package com.rentify.contactService.constants;

public final class ContactConstants {

    private ContactConstants() {
        throw new IllegalStateException("Clase de constantes - no debe ser instanciada");
    }

    // ESTADOS DEL MENSAJE
    public static final class EstadoMensaje {
        public static final String PENDIENTE = "PENDIENTE";
        public static final String EN_PROCESO = "EN_PROCESO";
        public static final String RESUELTO = "RESUELTO";

        private EstadoMensaje() {}

        public static boolean esValido(String estado) {
            return PENDIENTE.equals(estado) ||
                    EN_PROCESO.equals(estado) ||
                    RESUELTO.equals(estado);
        }
    }

    // ROLES Y PERMISOS
    public static final class Roles {
        public static final String ADMIN = "ADMIN";
        public static final String PROPIETARIO = "PROPIETARIO";
        public static final String ARRIENDATARIO = "ARRIENDATARIO";

        private Roles() {}

        public static boolean puedeResponder(String rol) {
            return ADMIN.equals(rol);
        }

        public static boolean puedeCrearMensaje(String rol) {
            return true; // Todos pueden crear mensajes
        }
    }

    // LÍMITES DE NEGOCIO
    public static final class Limites {
        public static final int MAX_MENSAJES_PENDIENTES_POR_USUARIO = 5;
        public static final int MIN_LONGITUD_MENSAJE = 10;
        public static final int MAX_LONGITUD_MENSAJE = 5000;
        public static final int MAX_LONGITUD_ASUNTO = 200;
        public static final int TIMEOUT_SECONDS = 5;

        private Limites() {}
    }

    // MENSAJES DE ERROR
    public static final class Mensajes {
        public static final String USUARIO_NO_EXISTE = "El usuario con ID %d no existe";
        public static final String MENSAJE_NO_ENCONTRADO = "El mensaje con ID %d no fue encontrado";
        public static final String ESTADO_INVALIDO = "El estado '%s' no es válido. Estados válidos: PENDIENTE, EN_PROCESO, RESUELTO";
        public static final String MAX_MENSAJES_PENDIENTES = "Ha alcanzado el límite de %d mensajes pendientes. Por favor espere respuesta.";
        public static final String MENSAJE_MUY_CORTO = "El mensaje debe tener al menos %d caracteres";
        public static final String MENSAJE_MUY_LARGO = "El mensaje no puede exceder %d caracteres";
        public static final String SOLO_ADMIN_PUEDE_RESPONDER = "Solo los administradores pueden responder mensajes";
        public static final String EMAIL_INVALIDO = "El formato del email es inválido";
        public static final String CAMPO_REQUERIDO = "El campo %s es obligatorio";

        private Mensajes() {}
    }

    // MENSAJES DE ÉXITO
    public static final class MensajesExito {
        public static final String MENSAJE_CREADO = "Su mensaje ha sido recibido. Le responderemos pronto.";
        public static final String MENSAJE_ACTUALIZADO = "El mensaje ha sido actualizado correctamente";
        public static final String RESPUESTA_ENVIADA = "La respuesta ha sido enviada exitosamente";

        private MensajesExito() {}
    }
}