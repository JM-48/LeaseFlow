package com.rentify.documentService.config;

import com.rentify.documentService.model.Documento;
import com.rentify.documentService.repository.DocumentoRepository;
import com.rentify.documentService.repository.EstadoRepository;
import com.rentify.documentService.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.util.Date;

/**
 * Inicializador de datos de prueba para Document Service.
 *
 * IMPORTANTE:
 * - Solo se ejecuta en perfil "dev" o "test"
 * - Se puede deshabilitar con: app.init.load-test-data=false
 * - Requiere que usuarios existan en User Service (IDs 1-5)
 *
 * Para activar:
 * 1. En application.properties agregar: spring.profiles.active=dev
 * 2. O agregar: app.init.load-test-data=true
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"}) // Solo en desarrollo o testing
@ConditionalOnProperty(name = "app.init.load-test-data", havingValue = "true", matchIfMissing = false)
public class TestDataInitializer {

    private final DocumentoRepository documentoRepository;
    private final EstadoRepository estadoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Bean
    @Order(10) // Se ejecuta después de los datos maestros
    public CommandLineRunner loadTestData() {
        return args -> {
            log.info("════════════════════════════════════════════════════════");
            log.info("🧪 CARGANDO DATOS DE PRUEBA - DOCUMENT SERVICE");
            log.info("════════════════════════════════════════════════════════");

            if (documentoRepository.count() > 0) {
                log.info("⚠️  Ya existen documentos en la base de datos");
                log.info("   Total documentos: {}", documentoRepository.count());
                log.info("   Saltando carga de datos de prueba");
                return;
            }

            log.info("📝 Creando documentos de prueba...");

            // Obtener IDs de estados y tipos de documento
            Long estadoPendiente = estadoRepository.findByNombre("PENDIENTE")
                    .orElseThrow(() -> new RuntimeException("Estado PENDIENTE no encontrado"))
                    .getId();

            Long estadoAceptado = estadoRepository.findByNombre("ACEPTADO")
                    .orElseThrow(() -> new RuntimeException("Estado ACEPTADO no encontrado"))
                    .getId();

            Long estadoEnRevision = estadoRepository.findByNombre("EN_REVISION")
                    .orElseThrow(() -> new RuntimeException("Estado EN_REVISION no encontrado"))
                    .getId();

            Long tipoDNI = tipoDocumentoRepository.findByNombre("DNI")
                    .orElseThrow(() -> new RuntimeException("Tipo DNI no encontrado"))
                    .getId();

            Long tipoLiquidacion = tipoDocumentoRepository.findByNombre("LIQUIDACION_SUELDO")
                    .orElseThrow(() -> new RuntimeException("Tipo LIQUIDACION_SUELDO no encontrado"))
                    .getId();

            Long tipoAntecedentes = tipoDocumentoRepository.findByNombre("CERTIFICADO_ANTECEDENTES")
                    .orElseThrow(() -> new RuntimeException("Tipo CERTIFICADO_ANTECEDENTES no encontrado"))
                    .getId();

            Long tipoAFP = tipoDocumentoRepository.findByNombre("CERTIFICADO_AFP")
                    .orElseThrow(() -> new RuntimeException("Tipo CERTIFICADO_AFP no encontrado"))
                    .getId();

            Long tipoContrato = tipoDocumentoRepository.findByNombre("CONTRATO_TRABAJO")
                    .orElseThrow(() -> new RuntimeException("Tipo CONTRATO_TRABAJO no encontrado"))
                    .getId();

            // ============================================================
            // USUARIO 1: da.olaver@duocuc.cl (ADMIN)
            // ============================================================
            log.info("   📄 Creando documentos para Usuario 1 (ADMIN)...");

            documentoRepository.save(Documento.builder()
                    .nombre("DNI_Diego_Olaver.pdf")
                    .usuarioId(1L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoDNI)
                    .fechaSubido(new Date())
                    .build());

            documentoRepository.save(Documento.builder()
                    .nombre("Liquidacion_Sueldo_Octubre_2025.pdf")
                    .usuarioId(1L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoLiquidacion)
                    .fechaSubido(new Date())
                    .build());

            documentoRepository.save(Documento.builder()
                    .nombre("Certificado_Antecedentes_2025.pdf")
                    .usuarioId(1L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoAntecedentes)
                    .fechaSubido(new Date())
                    .build());

            // ============================================================
            // USUARIO 2: fs.gonzalez@duocuc.cl (PROPIETARIO)
            // ============================================================
            log.info("   📄 Creando documentos para Usuario 2 (PROPIETARIO)...");

            documentoRepository.save(Documento.builder()
                    .nombre("DNI_Fabian_Gonzalez.pdf")
                    .usuarioId(2L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoDNI)
                    .fechaSubido(new Date())
                    .build());

            documentoRepository.save(Documento.builder()
                    .nombre("Certificado_Propiedad_Casa_Maipu.pdf")
                    .usuarioId(2L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoContrato)
                    .fechaSubido(new Date())
                    .build());

            // ============================================================
            // USUARIO 3: juan.perez@email.com (ARRIENDATARIO)
            // ============================================================
            log.info("   📄 Creando documentos para Usuario 3 (ARRIENDATARIO)...");

            documentoRepository.save(Documento.builder()
                    .nombre("DNI_Juan_Perez.pdf")
                    .usuarioId(3L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoDNI)
                    .fechaSubido(new Date())
                    .build());

            documentoRepository.save(Documento.builder()
                    .nombre("Liquidacion_Sueldo_Noviembre_2025.pdf")
                    .usuarioId(3L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoLiquidacion)
                    .fechaSubido(new Date())
                    .build());

            documentoRepository.save(Documento.builder()
                    .nombre("Certificado_AFP_2025.pdf")
                    .usuarioId(3L)
                    .estadoId(estadoPendiente)
                    .tipoDocId(tipoAFP)
                    .fechaSubido(new Date())
                    .build());

            documentoRepository.save(Documento.builder()
                    .nombre("Contrato_Trabajo_Empresa_XYZ.pdf")
                    .usuarioId(3L)
                    .estadoId(estadoEnRevision)
                    .tipoDocId(tipoContrato)
                    .fechaSubido(new Date())
                    .build());

            // ============================================================
            // USUARIO 4: maria.lopez@duoc.cl (ARRIENDATARIO)
            // ============================================================
            log.info("   📄 Creando documentos para Usuario 4 (ARRIENDATARIO)...");

            documentoRepository.save(Documento.builder()
                    .nombre("DNI_Maria_Lopez.pdf")
                    .usuarioId(4L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoDNI)
                    .fechaSubido(new Date())
                    .build());

            documentoRepository.save(Documento.builder()
                    .nombre("Liquidacion_Sueldo_Octubre_2025.pdf")
                    .usuarioId(4L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoLiquidacion)
                    .fechaSubido(new Date())
                    .build());

            documentoRepository.save(Documento.builder()
                    .nombre("Certificado_Antecedentes_Maria.pdf")
                    .usuarioId(4L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoAntecedentes)
                    .fechaSubido(new Date())
                    .build());

            // ============================================================
            // USUARIO 5: pedro.ramirez@email.com (PROPIETARIO)
            // ============================================================
            log.info("   📄 Creando documentos para Usuario 5 (PROPIETARIO)...");

            documentoRepository.save(Documento.builder()
                    .nombre("DNI_Pedro_Ramirez.pdf")
                    .usuarioId(5L)
                    .estadoId(estadoAceptado)
                    .tipoDocId(tipoDNI)
                    .fechaSubido(new Date())
                    .build());

            documentoRepository.save(Documento.builder()
                    .nombre("Certificado_Antecedentes_Pedro.pdf")
                    .usuarioId(5L)
                    .estadoId(estadoPendiente)
                    .tipoDocId(tipoAntecedentes)
                    .fechaSubido(new Date())
                    .build());

            long totalDocumentos = documentoRepository.count();

            log.info("✅ Datos de prueba cargados exitosamente");
            log.info("   Total documentos creados: {}", totalDocumentos);
            log.info("════════════════════════════════════════════════════════");

            // Resumen por usuario
            for (long userId = 1L; userId <= 5L; userId++) {
                long count = documentoRepository.countByUsuarioId(userId);
                if (count > 0) {
                    log.info("   Usuario {}: {} documentos", userId, count);
                }
            }

            log.info("════════════════════════════════════════════════════════");
        };
    }
}