package com.rentify.userservice.constants;

/**
 * Constantes del sistema User Service
 * Define roles, estados, validaciones y mensajes de error
 */
public final class UserConstants {

    private UserConstants() {
        throw new IllegalStateException("Clase de constantes - no debe ser instanciada");
    }

    /**
     * Roles del sistema Rentify
     */
    public static final class Roles {
        public static final String ADMIN = "ADMIN";
        public static final String PROPIETARIO = "PROPIETARIO";
        public static final String ARRIENDATARIO = "ARRIENDATARIO";

        private Roles() {}

        /**
         * Valida si un rol es válido en el sistema
         */
        public static boolean esValido(String rol) {
            return ADMIN.equals(rol) ||
                    PROPIETARIO.equals(rol) ||
                    ARRIENDATARIO.equals(rol);
        }

        /**
         * Obtiene el ID del rol por nombre
         */
        public static Long getIdPorNombre(String nombre) {
            return switch (nombre) {
                case ADMIN -> 1L;
                case PROPIETARIO -> 2L;
                case ARRIENDATARIO -> 3L;
                default -> null;
            };
        }
    }

    /**
     * Estados de usuario
     */
    public static final class Estados {
        public static final Long ACTIVO = 1L;
        public static final Long INACTIVO = 2L;
        public static final Long SUSPENDIDO = 3L;

        public static final String ACTIVO_NOMBRE = "ACTIVO";
        public static final String INACTIVO_NOMBRE = "INACTIVO";
        public static final String SUSPENDIDO_NOMBRE = "SUSPENDIDO";

        private Estados() {}

        public static boolean esValido(Long estadoId) {
            return ACTIVO.equals(estadoId) ||
                    INACTIVO.equals(estadoId) ||
                    SUSPENDIDO.equals(estadoId);
        }
    }

    /**
     * Validaciones de negocio
     */
    public static final class Validaciones {
        public static final int EDAD_MINIMA = 18;
        public static final int MAX_INTENTOS_LOGIN = 3;
        public static final int LONGITUD_CODIGO_REF = 9;
        public static final String REGEX_RUT = "^\\d{7,8}-[\\dkK]$";
        public static final String DOMINIO_DUOC = "@duoc.cl";
        public static final int MIN_LENGTH_PASSWORD = 8;
        public static final int PUNTOS_INICIALES = 0;
        public static final int PUNTOS_POR_REFERIDO = 100;

        private Validaciones() {}
    }

    /**
     * Mensajes de error del sistema
     */
    public static final class Mensajes {
        // Errores de usuario
        public static final String USUARIO_NO_ENCONTRADO = "Usuario con ID %d no encontrado";
        public static final String USUARIO_EMAIL_NO_ENCONTRADO = "Usuario con email %s no encontrado";
        public static final String EMAIL_DUPLICADO = "El email %s ya está registrado";
        public static final String RUT_DUPLICADO = "El RUT %s ya está registrado";
        public static final String EDAD_INSUFICIENTE = "Debe ser mayor de %d años para registrarse";

        // Errores de rol
        public static final String ROL_NO_ENCONTRADO = "Rol con ID %d no encontrado";
        public static final String ROL_NOMBRE_NO_ENCONTRADO = "Rol %s no encontrado";
        public static final String ROL_INVALIDO = "El rol %s no es válido. Roles permitidos: ADMIN, PROPIETARIO, ARRIENDATARIO";
        public static final String ROL_DUPLICADO = "Ya existe un rol con el nombre %s";

        // Errores de autenticación
        public static final String CREDENCIALES_INVALIDAS = "Email o contraseña incorrectos";
        public static final String CUENTA_INACTIVA = "La cuenta está inactiva. Contacte al administrador";
        public static final String CUENTA_SUSPENDIDA = "La cuenta está suspendida. Contacte al administrador";

        // Errores de estado
        public static final String ESTADO_INVALIDO = "El estado con ID %d no es válido";

        // Errores de código de referido
        public static final String CODIGO_REF_NO_ENCONTRADO = "Código de referido %s no encontrado";
        public static final String CODIGO_REF_DUPLICADO = "Error al generar código de referido único";

        private Mensajes() {}
    }

    /**
     * Endpoints de la API
     */
    public static final class Endpoints {
        public static final String BASE_USUARIOS = "/api/usuarios";
        public static final String BASE_ROLES = "/api/roles";
        public static final String BASE_AUTH = "/api/auth";

        private Endpoints() {}
    }
}