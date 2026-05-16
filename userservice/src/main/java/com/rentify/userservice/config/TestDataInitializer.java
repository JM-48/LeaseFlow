package com.rentify.userservice.config;

import com.rentify.userservice.model.Usuario;
import com.rentify.userservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Inicializador de datos de prueba para User Service
 *
 * Carga 5 usuarios de ejemplo para desarrollo y testing:
 * - 1 ADMIN (Daniel Olivares)
 * - 2 PROPIETARIOS (Francisco González, Pedro Ramírez)
 * - 2 ARRIENDATARIOS (Juan Pérez, María López)
 *
 * Se ejecuta solo en perfiles 'dev' o 'test' y si:
 * app.init.load-test-data=true
 *
 * @author Rentify Team
 * @version 1.0
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"})
@ConditionalOnProperty(
        name = "app.init.load-test-data",
        havingValue = "true",
        matchIfMissing = false
)
public class TestDataInitializer {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga usuarios de prueba
     *
     * IDs fijos para facilitar testing:
     * - ID 1: Daniel Olivares (ADMIN, DuocUC VIP)
     * - ID 2: Francisco González (PROPIETARIO, DuocUC VIP)
     * - ID 3: Juan Pérez (ARRIENDATARIO)
     * - ID 4: María López (ARRIENDATARIO, DuocUC VIP)
     * - ID 5: Pedro Ramírez (PROPIETARIO)
     *
     * @return CommandLineRunner con orden 10
     */
    @Bean
    @Order(10)
    @Transactional
    public CommandLineRunner loadTestUsers() {
        return args -> {
            if (usuarioRepository.count() == 0) {
                log.info("🔄 Verificando usuarios de prueba...");
                log.info("📝 Creando usuarios de prueba...");

                // 1. ADMIN - Daniel Olivares (DuocUC VIP)
                Usuario admin = Usuario.builder()
                        .pnombre("David")
                        .snombre("Ignacio")
                        .papellido("Olave")
                        .fnacimiento(LocalDate.of(1996, 11, 12))
                        .email("da.olaver@duocuc.cl")
                        .rut("19430962-7")
                        .ntelefono("+56941747707")
                        .duocVip(true)  // Correo @duocuc.cl
                        .clave("Admin123!")  // Contraseña simple para testing
                        .puntos(0)
                        .codigoRef("ADMIN001")
                        .fcreacion(LocalDate.now())
                        .factualizacion(LocalDate.now())
                        .estadoId(1L)  // ACTIVO
                        .rolId(1L)     // ADMIN
                        .build();

                // 2. PROPIETARIO - Francisco González (DuocUC VIP)
                Usuario propietario1 = Usuario.builder()
                        .pnombre("Fernanda")
                        .snombre("Santiago")
                        .papellido("Gonzalez")
                        .fnacimiento(LocalDate.of(1988, 7, 20))
                        .email("fs.gonzalez@duocuc.cl")
                        .rut("98765432-1")
                        .ntelefono("+56987654321")
                        .duocVip(true)  // Correo @duocuc.cl
                        .clave("Miauu123!")
                        .puntos(0)
                        .codigoRef("PROP001")
                        .fcreacion(LocalDate.now())
                        .factualizacion(LocalDate.now())
                        .estadoId(1L)  // ACTIVO
                        .rolId(2L)     // PROPIETARIO
                        .build();

                // 3. ARRIENDATARIO - Juan Pérez (No VIP)
                Usuario arriendatario1 = Usuario.builder()
                        .pnombre("Juan")
                        .snombre("Carlos")
                        .papellido("Pérez")
                        .fnacimiento(LocalDate.of(1995, 5, 15))
                        .email("juan.perez@email.com")
                        .rut("11111111-1")
                        .ntelefono("+56911111111")
                        .duocVip(false)  // Correo normal
                        .clave("Miauu123!")  // Contraseña de 11 caracteres
                        .puntos(0)
                        .codigoRef("ABC123XYZ")
                        .fcreacion(LocalDate.now())
                        .factualizacion(LocalDate.now())
                        .estadoId(1L)  // ACTIVO
                        .rolId(3L)     // ARRIENDATARIO
                        .build();

                // 4. ARRIENDATARIO - María López (DuocUC VIP)
                Usuario arriendatario2 = Usuario.builder()
                        .pnombre("María")
                        .snombre("José")
                        .papellido("López")
                        .fnacimiento(LocalDate.of(1992, 11, 8))
                        .email("maria.lopez@duoc.cl")
                        .rut("22222222-2")
                        .ntelefono("+56922222222")
                        .duocVip(true)  // Correo @duoc.cl
                        .clave("Miau123!")
                        .puntos(100)  // Usuario con puntos
                        .codigoRef("DUOC12345")
                        .fcreacion(LocalDate.now())
                        .factualizacion(LocalDate.now())
                        .estadoId(1L)  // ACTIVO
                        .rolId(3L)     // ARRIENDATARIO
                        .build();

                // 5. PROPIETARIO - Pedro Ramírez (No VIP)
                Usuario propietario2 = Usuario.builder()
                        .pnombre("Pedro")
                        .snombre("Antonio")
                        .papellido("Ramírez")
                        .fnacimiento(LocalDate.of(1985, 2, 28))
                        .email("pedro.ramirez@email.com")
                        .rut("33333333-3")
                        .ntelefono("+56933333333")
                        .duocVip(false)  // Correo normal
                        .clave("Miau123!")
                        .puntos(50)  // Usuario con puntos
                        .codigoRef("PROP54321")
                        .fcreacion(LocalDate.now())
                        .factualizacion(LocalDate.now())
                        .estadoId(1L)  // ACTIVO
                        .rolId(2L)     // PROPIETARIO
                        .build();

                // Guardar todos los usuarios
                usuarioRepository.save(admin);
                usuarioRepository.save(propietario1);
                usuarioRepository.save(arriendatario1);
                usuarioRepository.save(arriendatario2);
                usuarioRepository.save(propietario2);

                log.info("✅ 5 usuarios de prueba creados exitosamente");
                log.info("   - 1 ADMIN (DuocUC VIP)");
                log.info("   - 2 PROPIETARIOS (1 VIP, 1 normal)");
                log.info("   - 2 ARRIENDATARIOS (1 VIP, 1 normal)");
                log.info("");
                log.info("📧 Usuarios disponibles para testing:");
                log.info("   [ADMIN]         da.olaver@duocuc.cl      / Admin123!");
                log.info("   [PROPIETARIO]   fs.gonzalez@duocuc.cl    / Miau123!");
                log.info("   [ARRIENDATARIO] juan.perez@email.com     / Miau123!");
                log.info("   [ARRIENDATARIO] maria.lopez@duoc.cl      / Miau123!");
                log.info("   [PROPIETARIO]   pedro.ramirez@email.com  / Miau123!");
                log.info("");
                log.info("🌟 Usuarios DuocUC VIP: 3 (20% descuento en comisiones)");
            } else {
                log.info("✅ Usuarios ya existen en la base de datos ({})", usuarioRepository.count());
            }
        };
    }
}