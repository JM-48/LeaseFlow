package com.rentify.documentService.config;

import com.rentify.documentService.model.Documento;
import com.rentify.documentService.model.Estado;
import com.rentify.documentService.model.TipoDocumento;
import com.rentify.documentService.repository.DocumentoRepository;
import com.rentify.documentService.repository.EstadoRepository;
import com.rentify.documentService.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Inicializador de documentos de prueba para Document Service.
 * Se ejecuta automaticamente al iniciar la aplicacion.
 * Solo puebla la tabla si esta vacia (no duplica datos).
 *
 * IMPORTANTE: Se ejecuta DESPUES de DataInitializerConfig (Order 2)
 * para garantizar que estados y tipos de documento ya existan.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class DocumentoDataInitializer implements ApplicationRunner {

    private final DocumentoRepository documentoRepository;
    private final EstadoRepository estadoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================================");
        log.info("VERIFICANDO DOCUMENTOS DE PRUEBA - DOCUMENT SERVICE");
        log.info("========================================================");

        if (documentoRepository.count() > 0) {
            log.info("Ya existen {} documentos en la base de datos. Saltando inicializacion.",
                    documentoRepository.count());
            return;
        }

        // Verificar que existan estados y tipos de documento
        if (estadoRepository.count() == 0 || tipoDocumentoRepository.count() == 0) {
            log.error("No existen estados o tipos de documento. No se pueden crear documentos de prueba.");
            return;
        }

        log.info("Creando documentos de prueba...");

        try {
            // Obtener IDs de estados
            Long estadoPendiente = getEstadoId("PENDIENTE");
            Long estadoAceptado = getEstadoId("ACEPTADO");
            Long estadoEnRevision = getEstadoId("EN_REVISION");

            // Obtener IDs de tipos de documento
            Long tipoDNI = getTipoDocId("DNI");
            Long tipoLiquidacion = getTipoDocId("LIQUIDACION_SUELDO");
            Long tipoAntecedentes = getTipoDocId("CERTIFICADO_ANTECEDENTES");
            Long tipoAFP = getTipoDocId("CERTIFICADO_AFP");
            Long tipoContrato = getTipoDocId("CONTRATO_TRABAJO");

            // ============================================================
            // USUARIO 1: da.olaver@duocuc.cl (ADMIN)
            // ============================================================
            log.info("Creando documentos para Usuario 1 (ADMIN)...");

            crearDocumento("DNI_Diego_Olaver.pdf", 1L, estadoAceptado, tipoDNI);
            crearDocumento("Liquidacion_Sueldo_Octubre_2025.pdf", 1L, estadoAceptado, tipoLiquidacion);
            crearDocumento("Certificado_Antecedentes_2025.pdf", 1L, estadoAceptado, tipoAntecedentes);

            // ============================================================
            // USUARIO 2: fs.gonzalez@duocuc.cl (PROPIETARIO)
            // ============================================================
            log.info("Creando documentos para Usuario 2 (PROPIETARIO)...");

            crearDocumento("DNI_Fabian_Gonzalez.pdf", 2L, estadoAceptado, tipoDNI);
            crearDocumento("Certificado_Propiedad_Casa_Maipu.pdf", 2L, estadoAceptado, tipoContrato);

            // ============================================================
            // USUARIO 3: juan.perez@email.com (ARRIENDATARIO)
            // ============================================================
            log.info("Creando documentos para Usuario 3 (ARRIENDATARIO)...");

            crearDocumento("DNI_Juan_Perez.pdf", 3L, estadoAceptado, tipoDNI);
            crearDocumento("Liquidacion_Sueldo_Noviembre_2025.pdf", 3L, estadoAceptado, tipoLiquidacion);
            crearDocumento("Certificado_AFP_2025.pdf", 3L, estadoPendiente, tipoAFP);
            crearDocumento("Contrato_Trabajo_Empresa_XYZ.pdf", 3L, estadoEnRevision, tipoContrato);

            // ============================================================
            // USUARIO 4: maria.lopez@duoc.cl (ARRIENDATARIO)
            // ============================================================
            log.info("Creando documentos para Usuario 4 (ARRIENDATARIO)...");

            crearDocumento("DNI_Maria_Lopez.pdf", 4L, estadoAceptado, tipoDNI);
            crearDocumento("Liquidacion_Sueldo_Octubre_2025.pdf", 4L, estadoAceptado, tipoLiquidacion);
            crearDocumento("Certificado_Antecedentes_Maria.pdf", 4L, estadoAceptado, tipoAntecedentes);

            // ============================================================
            // USUARIO 5: pedro.ramirez@email.com (PROPIETARIO)
            // ============================================================
            log.info("Creando documentos para Usuario 5 (PROPIETARIO)...");

            crearDocumento("DNI_Pedro_Ramirez.pdf", 5L, estadoAceptado, tipoDNI);
            crearDocumento("Certificado_Antecedentes_Pedro.pdf", 5L, estadoPendiente, tipoAntecedentes);

            // Resumen
            log.info("========================================================");
            log.info("DOCUMENTOS DE PRUEBA CREADOS EXITOSAMENTE");
            log.info("========================================================");
            log.info("Total documentos creados: {}", documentoRepository.count());

            // Resumen por usuario
            for (long userId = 1L; userId <= 5L; userId++) {
                long count = documentoRepository.countByUsuarioId(userId);
                if (count > 0) {
                    log.info("  - Usuario {}: {} documentos", userId, count);
                }
            }
            log.info("========================================================");

        } catch (Exception e) {
            log.error("Error al crear documentos de prueba: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene el ID de un estado por nombre.
     */
    private Long getEstadoId(String nombre) {
        Estado estado = estadoRepository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado: " + nombre));
        return estado.getId();
    }

    /**
     * Obtiene el ID de un tipo de documento por nombre.
     */
    private Long getTipoDocId(String nombre) {
        TipoDocumento tipo = tipoDocumentoRepository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado: " + nombre));
        return tipo.getId();
    }

    /**
     * Crea y guarda un documento.
     */
    private void crearDocumento(String nombre, Long usuarioId, Long estadoId, Long tipoDocId) {
        Documento documento = new Documento();
        documento.setNombre(nombre);
        documento.setUsuarioId(usuarioId);
        documento.setEstadoId(estadoId);
        documento.setTipoDocId(tipoDocId);
        documento.setFechaSubido(new Date());

        documentoRepository.save(documento);
        log.debug("  - Creado: {} (Usuario: {}, Estado: {}, Tipo: {})",
                nombre, usuarioId, estadoId, tipoDocId);
    }
}