package com.rentify.reviewService.constants;

/**
 * Constantes utilizadas en el Review Service.
 * Contiene valores estáticos para validaciones, mensajes y configuraciones.
 */
public final class ReviewConstants {

    private ReviewConstants() {
        throw new IllegalStateException("Clase de constantes - no debe ser instanciada");
    }

    // ===============================================
    // TIPOS DE RESEÑA
    // ===============================================
    public static final class TipoResena {
        public static final String RESENA_PROPIEDAD = "RESENA_PROPIEDAD";
        public static final String RESENA_USUARIO = "RESENA_USUARIO";
        public static final String RESENA_PROPIETARIO = "RESENA_PROPIETARIO";
        public static final String RESENA_ARRIENDATARIO = "RESENA_ARRIENDATARIO";

        private TipoResena() {}

        public static boolean esValido(String tipo) {
            return RESENA_PROPIEDAD.equals(tipo) ||
                    RESENA_USUARIO.equals(tipo) ||
                    RESENA_PROPIETARIO.equals(tipo) ||
                    RESENA_ARRIENDATARIO.equals(tipo);
        }
    }

    // ===============================================
    // LÍMITES Y VALIDACIONES
    // ===============================================
    public static final class Limites {
        public static final int PUNTAJE_MINIMO = 1;
        public static final int PUNTAJE_MAXIMO = 10;
        public static final int MAX_LONGITUD_COMENTARIO = 500;
        public static final int MIN_LONGITUD_COMENTARIO = 10;
        public static final int MAX_RESENAS_POR_USUARIO_PROPIEDAD = 1;
        public static final int TIMEOUT_SECONDS = 5;

        private Limites() {}

        public static boolean esPuntajeValido(int puntaje) {
            return puntaje >= PUNTAJE_MINIMO && puntaje <= PUNTAJE_MAXIMO;
        }
    }

    // ===============================================
    // ROLES (para validaciones)
    // ===============================================
    public static final class Roles {
        public static final String ADMIN = "ADMIN";
        public static final String PROPIETARIO = "PROPIETARIO";
        public static final String ARRIENDATARIO = "ARRIENDATARIO";

        private Roles() {}

        public static boolean puedeCrearResena(String rol) {
            return ARRIENDATARIO.equals(rol) ||
                    PROPIETARIO.equals(rol) ||
                    ADMIN.equals(rol);
        }

        public static boolean puedeEliminarResena(String rol) {
            return ADMIN.equals(rol);
        }
    }

    // ===============================================
    // MENSAJES DE ERROR
    // ===============================================
    public static final class Mensajes {
        // Errores de entidades no encontradas
        public static final String RESENA_NO_ENCONTRADA = "La reseña con ID %d no existe";
        public static final String TIPO_RESENA_NO_ENCONTRADO = "El tipo de reseña con ID %d no existe";
        public static final String USUARIO_NO_EXISTE = "El usuario con ID %d no existe";
        public static final String PROPIEDAD_NO_EXISTE = "La propiedad con ID %d no existe";

        // Errores de validación de negocio
        public static final String PUNTAJE_INVALIDO = "El puntaje debe estar entre %d y %d";
        public static final String COMENTARIO_MUY_CORTO = "El comentario debe tener al menos %d caracteres";
        public static final String COMENTARIO_MUY_LARGO = "El comentario no puede exceder %d caracteres";
        public static final String RESENA_DUPLICADA = "El usuario ya ha creado una reseña para esta propiedad";
        public static final String TIPO_RESENA_INVALIDO = "El tipo de reseña '%s' no es válido";
        public static final String ROL_INVALIDO_RESENA = "El usuario no tiene permisos para crear reseñas";
        public static final String USUARIO_NO_PUEDE_RESENAR_PROPIA_PROPIEDAD = "Un propietario no puede reseñar su propia propiedad";

        // Errores de microservicios
        public static final String ERROR_COMUNICACION_USER_SERVICE = "No se pudo verificar el usuario. Intente nuevamente";
        public static final String ERROR_COMUNICACION_PROPERTY_SERVICE = "No se pudo verificar la propiedad. Intente nuevamente";

        // Mensajes de éxito
        public static final String RESENA_CREADA = "Reseña creada exitosamente";
        public static final String RESENA_ELIMINADA = "Reseña eliminada exitosamente";

        private Mensajes() {}
    }

    // ===============================================
    // ESTADOS
    // ===============================================
    public static final class Estados {
        public static final String ACTIVA = "ACTIVA";
        public static final String BANEADA = "BANEADA";
        public static final String OCULTA = "OCULTA";

        private Estados() {}

        public static boolean esValido(String estado) {
            return ACTIVA.equals(estado) ||
                    BANEADA.equals(estado) ||
                    OCULTA.equals(estado);
        }
    }
}