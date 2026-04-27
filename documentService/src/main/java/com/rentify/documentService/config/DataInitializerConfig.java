package com.rentify.documentService.config;

import com.rentify.documentService.model.Estado;
import com.rentify.documentService.model.TipoDocumento;
import com.rentify.documentService.repository.EstadoRepository;
import com.rentify.documentService.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de datos maestros para Document Service.
 * Se ejecuta automaticamente al iniciar la aplicacion.
 * Solo puebla las tablas si estan vacias (no duplica datos).
 *
 * IMPORTANTE: Usa ApplicationRunner en lugar de CommandLineRunner
 * para garantizar que se ejecute despues de que Spring termine
 * de configurar todos los beans y la base de datos.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DataInitializerConfig implements ApplicationRunner {

    private final EstadoRepository estadoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================================");
        log.info("INICIANDO POBLADO DE DATOS MAESTROS - DOCUMENT SERVICE");
        log.info("========================================================");

        initEstados();
        initTiposDocumento();
        printSummary();
    }

    /**
     * Inicializa los estados de documentos.
     * Estados: PENDIENTE, ACEPTADO, RECHAZADO, EN_REVISION
     */
    private void initEstados() {
        log.info("Verificando estados de documentos...");

        long count = estadoRepository.count();
        if (count > 0) {
            log.info("Estados ya existen ({} registros). Saltando inicializacion.", count);
            return;
        }

        log.info("Creando estados de documentos...");

        // Estado 1: PENDIENTE
        Estado pendiente = new Estado();
        pendiente.setNombre("PENDIENTE");
        estadoRepository.save(pendiente);
        log.info("  - Creado: PENDIENTE (ID: {})", pendiente.getId());

        // Estado 2: ACEPTADO
        Estado aceptado = new Estado();
        aceptado.setNombre("ACEPTADO");
        estadoRepository.save(aceptado);
        log.info("  - Creado: ACEPTADO (ID: {})", aceptado.getId());

        // Estado 3: RECHAZADO
        Estado rechazado = new Estado();
        rechazado.setNombre("RECHAZADO");
        estadoRepository.save(rechazado);
        log.info("  - Creado: RECHAZADO (ID: {})", rechazado.getId());

        // Estado 4: EN_REVISION
        Estado enRevision = new Estado();
        enRevision.setNombre("EN_REVISION");
        estadoRepository.save(enRevision);
        log.info("  - Creado: EN_REVISION (ID: {})", enRevision.getId());

        log.info("Estados creados exitosamente: {} registros", estadoRepository.count());
    }

    /**
     * Inicializa los tipos de documentos.
     * Tipos: DNI, PASAPORTE, LIQUIDACION_SUELDO, CERTIFICADO_ANTECEDENTES, CERTIFICADO_AFP, CONTRATO_TRABAJO
     */
    private void initTiposDocumento() {
        log.info("Verificando tipos de documentos...");

        long count = tipoDocumentoRepository.count();
        if (count > 0) {
            log.info("Tipos de documento ya existen ({} registros). Saltando inicializacion.", count);
            return;
        }

        log.info("Creando tipos de documentos...");

        // Tipo 1: DNI
        TipoDocumento dni = new TipoDocumento();
        dni.setNombre("DNI");
        tipoDocumentoRepository.save(dni);
        log.info("  - Creado: DNI (ID: {})", dni.getId());

        // Tipo 2: PASAPORTE
        TipoDocumento pasaporte = new TipoDocumento();
        pasaporte.setNombre("PASAPORTE");
        tipoDocumentoRepository.save(pasaporte);
        log.info("  - Creado: PASAPORTE (ID: {})", pasaporte.getId());

        // Tipo 3: LIQUIDACION_SUELDO
        TipoDocumento liquidacion = new TipoDocumento();
        liquidacion.setNombre("LIQUIDACION_SUELDO");
        tipoDocumentoRepository.save(liquidacion);
        log.info("  - Creado: LIQUIDACION_SUELDO (ID: {})", liquidacion.getId());

        // Tipo 4: CERTIFICADO_ANTECEDENTES
        TipoDocumento antecedentes = new TipoDocumento();
        antecedentes.setNombre("CERTIFICADO_ANTECEDENTES");
        tipoDocumentoRepository.save(antecedentes);
        log.info("  - Creado: CERTIFICADO_ANTECEDENTES (ID: {})", antecedentes.getId());

        // Tipo 5: CERTIFICADO_AFP
        TipoDocumento afp = new TipoDocumento();
        afp.setNombre("CERTIFICADO_AFP");
        tipoDocumentoRepository.save(afp);
        log.info("  - Creado: CERTIFICADO_AFP (ID: {})", afp.getId());

        // Tipo 6: CONTRATO_TRABAJO
        TipoDocumento contrato = new TipoDocumento();
        contrato.setNombre("CONTRATO_TRABAJO");
        tipoDocumentoRepository.save(contrato);
        log.info("  - Creado: CONTRATO_TRABAJO (ID: {})", contrato.getId());

        log.info("Tipos de documento creados exitosamente: {} registros", tipoDocumentoRepository.count());
    }

    /**
     * Imprime resumen de la inicializacion.
     */
    private void printSummary() {
        log.info("========================================================");
        log.info("INICIALIZACION COMPLETADA - DOCUMENT SERVICE");
        log.info("========================================================");
        log.info("Resumen de datos:");
        log.info("  - Estados: {} registros", estadoRepository.count());
        log.info("  - Tipos de Documento: {} registros", tipoDocumentoRepository.count());
        log.info("========================================================");
        log.info("Document Service listo para recibir peticiones");
        log.info("Puerto: 8083");
        log.info("Swagger UI: http://localhost:8083/swagger-ui/index.html");
        log.info("========================================================");
    }
}