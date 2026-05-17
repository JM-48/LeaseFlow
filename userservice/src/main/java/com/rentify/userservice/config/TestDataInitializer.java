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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
    private final BCryptPasswordEncoder passwordEncoder;

    @Bean
    @Order(10)
    @Transactional
    public CommandLineRunner loadTestUsers() {
        return args -> {
            if (usuarioRepository.count() == 0) {
                log.info("Verificando usuarios de prueba...");
                log.info("Creando usuarios de prueba...");

                Usuario admin = Usuario.builder()
                        .pnombre("David")
                        .snombre("Ignacio")
                        .papellido("Olave")
                        .fnacimiento(LocalDate.of(1996, 11, 12))
                        .email("da.olaver@duocuc.cl")
                        .rut("19430962-7")
                        .ntelefono("+56941747707")
                        .duocVip(true)
                        .clave(passwordEncoder.encode("Admin123!"))
                        .puntos(0)
                        .codigoRef("ADMIN001X")
                        .fcreacion(LocalDate.now())
                        .factualizacion(LocalDate.now())
                        .estadoId(1L)
                        .rolId(1L)
                        .build();

                Usuario propietario1 = Usuario.builder()
                        .pnombre("Fernanda")
                        .snombre("Santiago")
                        .papellido("Gonzalez")
                        .fnacimiento(LocalDate.of(1988, 7, 20))
                        .email("fs.gonzalez@duocuc.cl")
                        .rut("98765432-1")
                        .ntelefono("+56987654321")
                        .duocVip(true)
                        .clave(passwordEncoder.encode("Miauu123!"))
                        .puntos(0)
                        .codigoRef("PROP001XX")
                        .fcreacion(LocalDate.now())
                        .factualizacion(LocalDate.now())
                        .estadoId(1L)
                        .rolId(2L)
                        .build();

                Usuario arriendatario1 = Usuario.builder()
                        .pnombre("Juan")
                        .snombre("Carlos")
                        .papellido("Pérez")
                        .fnacimiento(LocalDate.of(1995, 5, 15))
                        .email("juan.perez@email.com")
                        .rut("11111111-1")
                        .ntelefono("+56911111111")
                        .duocVip(false)
                        .clave(passwordEncoder.encode("Miau123!"))
                        .puntos(0)
                        .codigoRef("ABC123XYZ")
                        .fcreacion(LocalDate.now())
                        .factualizacion(LocalDate.now())
                        .estadoId(1L)
                        .rolId(3L)
                        .build();

                Usuario arriendatario2 = Usuario.builder()
                        .pnombre("María")
                        .snombre("José")
                        .papellido("López")
                        .fnacimiento(LocalDate.of(1992, 11, 8))
                        .email("maria.lopez@duoc.cl")
                        .rut("22222222-2")
                        .ntelefono("+56922222222")
                        .duocVip(true)
                        .clave(passwordEncoder.encode("Miau123!"))
                        .puntos(100)
                        .codigoRef("DUOC12345")
                        .fcreacion(LocalDate.now())
                        .factualizacion(LocalDate.now())
                        .estadoId(1L)
                        .rolId(3L)
                        .build();

                Usuario propietario2 = Usuario.builder()
                        .pnombre("Pedro")
                        .snombre("Antonio")
                        .papellido("Ramírez")
                        .fnacimiento(LocalDate.of(1985, 2, 28))
                        .email("pedro.ramirez@email.com")
                        .rut("33333333-3")
                        .ntelefono("+56933333333")
                        .duocVip(false)
                        .clave(passwordEncoder.encode("Miau123!"))
                        .puntos(50)
                        .codigoRef("PROP54321")
                        .fcreacion(LocalDate.now())
                        .factualizacion(LocalDate.now())
                        .estadoId(1L)
                        .rolId(2L)
                        .build();

                usuarioRepository.save(admin);
                usuarioRepository.save(propietario1);
                usuarioRepository.save(arriendatario1);
                usuarioRepository.save(arriendatario2);
                usuarioRepository.save(propietario2);

                log.info("5 usuarios de prueba creados con contraseñas BCrypt (strength=12)");
                log.info("   [ADMIN]         da.olaver@duocuc.cl      / Admin123!");
                log.info("   [PROPIETARIO]   fs.gonzalez@duocuc.cl    / Miauu123!");
                log.info("   [ARRIENDATARIO] juan.perez@email.com     / Miau123!");
                log.info("   [ARRIENDATARIO] maria.lopez@duoc.cl      / Miau123!");
                log.info("   [PROPIETARIO]   pedro.ramirez@email.com  / Miau123!");
            } else {
                log.info("Usuarios ya existen en la base de datos ({})", usuarioRepository.count());
            }
        };
    }
}