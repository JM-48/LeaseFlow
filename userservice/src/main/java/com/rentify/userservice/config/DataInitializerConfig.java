package com.rentify.userservice.config;

import com.rentify.userservice.model.Estado;
import com.rentify.userservice.model.Rol;
import com.rentify.userservice.repository.EstadoRepository;
import com.rentify.userservice.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración para inicialización automática de datos maestros.
 * Puebla las tablas: rol, estado al iniciar la aplicación.
 *
 * ORDEN DE EJECUCIÓN:
 * 1. Roles (ADMIN, PROPIETARIO, ARRIENDATARIO)
 * 2. Estados (ACTIVO, INACTIVO, SUSPENDIDO)
 *
 * CARACTERÍSTICAS:
 * - Solo se ejecuta si las tablas están vacías
 * - IDs fijos para roles y estados (importante para el frontend)
 * - Logs informativos con emojis
 *
 * PARA DESHABILITAR:
 * - Configurar: app.init.populate-on-startup=false
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializerConfig {

    private final RolRepository rolRepository;
    private final EstadoRepository estadoRepository;

    /**
     * PASO 1: Inicializar Roles del Sistema
     *
     * IDs FIJOS (IMPORTANTE - NO CAMBIAR):
     * - ID 1: ADMIN
     * - ID 2: PROPIETARIO
     * - ID 3: ARRIENDATARIO
     *
     * Estos IDs son usados por el frontend y otros microservicios.
     */
    @Bean
    @Order(1)
    public CommandLineRunner initRoles() {
        return args -> {
            log.info("🔄 Verificando roles del sistema...");

            if (rolRepository.count() > 0) {
                log.info("✅ Roles ya existen en la base de datos. Total: {}", rolRepository.count());
                return;
            }

            log.info("📝 Creando roles del sistema...");

            List<Rol> roles = Arrays.asList(
                    Rol.builder().nombre("ADMIN").build(),
                    Rol.builder().nombre("PROPIETARIO").build(),
                    Rol.builder().nombre("ARRIENDATARIO").build()
            );

            rolRepository.saveAll(roles);

            log.info("✅ {} roles creados exitosamente", roles.size());
            log.info("   - ID 1: ADMIN (Administrador del sistema)");
            log.info("   - ID 2: PROPIETARIO (Dueño de propiedades)");
            log.info("   - ID 3: ARRIENDATARIO (Usuario que arrienda)");
        };
    }

    /**
     * PASO 2: Inicializar Estados del Sistema
     *
     * IDs FIJOS (IMPORTANTE - NO CAMBIAR):
     * - ID 1: ACTIVO
     * - ID 2: INACTIVO
     * - ID 3: SUSPENDIDO
     *
     * Estos IDs son usados por el frontend y otros microservicios.
     */
    @Bean
    @Order(2)
    public CommandLineRunner initEstados() {
        return args -> {
            log.info("🔄 Verificando estados del sistema...");

            if (estadoRepository.count() > 0) {
                log.info("✅ Estados ya existen en la base de datos. Total: {}", estadoRepository.count());
                return;
            }

            log.info("📝 Creando estados del sistema...");

            List<Estado> estados = Arrays.asList(
                    Estado.builder().nombre("ACTIVO").build(),
                    Estado.builder().nombre("INACTIVO").build(),
                    Estado.builder().nombre("SUSPENDIDO").build()
            );

            estadoRepository.saveAll(estados);

            log.info("✅ {} estados creados exitosamente", estados.size());
            log.info("   - ID 1: ACTIVO (Usuario activo)");
            log.info("   - ID 2: INACTIVO (Usuario inactivo)");
            log.info("   - ID 3: SUSPENDIDO (Usuario suspendido)");
        };
    }

    /**
     * PASO 3: Resumen de Inicialización
     */
    @Bean
    @Order(3)
    public CommandLineRunner printInitializationSummary() {
        return args -> {
            log.info("═══════════════════════════════════════════════════════════");
            log.info("🎉 INICIALIZACIÓN DE USER SERVICE COMPLETADA");
            log.info("═══════════════════════════════════════════════════════════");
            log.info("📊 RESUMEN DE DATOS MAESTROS:");
            log.info("   ✅ Roles:    {} registros", rolRepository.count());
            log.info("   ✅ Estados:  {} registros", estadoRepository.count());
            log.info("═══════════════════════════════════════════════════════════");
            log.info("🚀 User Service listo para registrar usuarios");
            log.info("📍 Swagger UI: http://localhost:8081/swagger-ui/index.html");
            log.info("═══════════════════════════════════════════════════════════");
        };
    }
}