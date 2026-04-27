package com.rentify.propertyservice.constants;

/**
 * Constantes utilizadas en el PropertyService.
 * Centraliza todos los valores constantes del sistema.
 */
public final class PropertyConstants {

    private PropertyConstants() {
        throw new IllegalStateException("Clase de constantes - no debe ser instanciada");
    }

    // ====== ESTADOS ======
    public static final class Estados {
        public static final String ACTIVA = "ACTIVA";
        public static final String INACTIVA = "INACTIVA";
        public static final String EN_REVISION = "EN_REVISION";
        public static final String ARRENDADA = "ARRENDADA";

        private Estados() {}

        public static boolean esValido(String estado) {
            return ACTIVA.equals(estado) ||
                    INACTIVA.equals(estado) ||
                    EN_REVISION.equals(estado) ||
                    ARRENDADA.equals(estado);
        }
    }

    // ====== DIVISAS ======
    public static final class Divisas {
        public static final String CLP = "CLP";
        public static final String USD = "USD";
        public static final String EUR = "EUR";

        private Divisas() {}

        public static boolean esValida(String divisa) {
            return CLP.equals(divisa) || USD.equals(divisa) || EUR.equals(divisa);
        }
    }

    // ====== LÍMITES ======
    public static final class Limites {
        public static final int MAX_FOTOS_POR_PROPIEDAD = 20;
        public static final long MAX_FILE_SIZE_MB = 10;
        public static final int MIN_M2 = 1;
        public static final int MAX_M2 = 10000;
        public static final int MIN_HABITACIONES = 0;
        public static final int MAX_HABITACIONES = 50;
        public static final int MIN_BANOS = 0;
        public static final int MAX_BANOS = 20;
        public static final int CODIGO_LENGTH = 10;

        private Limites() {}
    }

    // ====== FORMATOS DE ARCHIVO ======
    public static final class FormatosArchivo {
        public static final String[] IMAGENES_PERMITIDAS = {"image/jpeg", "image/jpg", "image/png", "image/webp"};

        private FormatosArchivo() {}

        public static boolean esFormatoValido(String contentType) {
            if (contentType == null) return false;
            for (String formato : IMAGENES_PERMITIDAS) {
                if (formato.equalsIgnoreCase(contentType)) {
                    return true;
                }
            }
            return false;
        }
    }

    // ====== MENSAJES DE ERROR ======
    public static final class Mensajes {
        // Propiedades
        public static final String PROPIEDAD_NO_ENCONTRADA = "La propiedad con ID %d no existe";
        public static final String CODIGO_DUPLICADO = "Ya existe una propiedad con el código %s";
        public static final String PRECIO_INVALIDO = "El precio mensual debe ser mayor a 0";
        public static final String M2_INVALIDO = "Los metros cuadrados deben estar entre %d y %d";
        public static final String HABITACIONES_INVALIDAS = "El número de habitaciones debe estar entre %d y %d";
        public static final String BANOS_INVALIDOS = "El número de baños debe estar entre %d y %d";
        public static final String DIVISA_INVALIDA = "La divisa %s no es válida. Use: CLP, USD o EUR";

        // Fotos
        public static final String FOTO_NO_ENCONTRADA = "La foto con ID %d no existe";
        public static final String MAX_FOTOS_ALCANZADO = "Se ha alcanzado el límite de %d fotos por propiedad";
        public static final String FORMATO_ARCHIVO_INVALIDO = "El formato de archivo no es válido. Use: JPG, PNG o WEBP";
        public static final String ARCHIVO_VACIO = "El archivo está vacío";
        public static final String ARCHIVO_MUY_GRANDE = "El archivo excede el tamaño máximo de %d MB";

        // Catálogos
        public static final String TIPO_NO_ENCONTRADO = "El tipo con ID %d no existe";
        public static final String COMUNA_NO_ENCONTRADA = "La comuna con ID %d no existe";
        public static final String REGION_NO_ENCONTRADA = "La región con ID %d no existe";
        public static final String CATEGORIA_NO_ENCONTRADA = "La categoría con ID %d no existe";

        // Validaciones de negocio
        public static final String PROPIEDAD_YA_ARRENDADA = "La propiedad ya está arrendada y no puede ser modificada";
        public static final String DATOS_INCOMPLETOS = "Faltan datos obligatorios para crear la propiedad";

        private Mensajes() {}
    }

    // ====== ROLES ======
    public static final class Roles {
        public static final String ADMIN = "ADMIN";
        public static final String PROPIETARIO = "PROPIETARIO";
        public static final String ARRIENDATARIO = "ARRIENDATARIO";

        private Roles() {}

        public static boolean puedeCrearPropiedad(String rol) {
            return ADMIN.equals(rol) || PROPIETARIO.equals(rol);
        }

        public static boolean puedeModificarPropiedad(String rol) {
            return ADMIN.equals(rol) || PROPIETARIO.equals(rol);
        }
    }
}