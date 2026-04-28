package com.rentify.contactService.config;

import com.rentify.contactService.model.MensajeContacto;
import com.rentify.contactService.repository.MensajeContactoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Inicializador de datos de prueba para Contact Service.
 * Se ejecuta automaticamente al iniciar la aplicacion.
 * Solo puebla la tabla si esta vacia (no duplica datos).
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DataInitializerConfig implements ApplicationRunner {

    private final MensajeContactoRepository mensajeContactoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================================");
        log.info("INICIANDO POBLADO DE DATOS - CONTACT SERVICE");
        log.info("========================================================");

        initMensajesContacto();
        printSummary();
    }

    /**
     * Inicializa mensajes de contacto de prueba.
     */
    private void initMensajesContacto() {
        log.info("Verificando mensajes de contacto...");

        long count = mensajeContactoRepository.count();
        if (count > 0) {
            log.info("Ya existen {} mensajes en la base de datos. Saltando inicializacion.", count);
            return;
        }

        log.info("Creando mensajes de contacto de prueba...");

        // ============================================================
        // MENSAJE 1: Consulta de usuario no autenticado - PENDIENTE
        // ============================================================
        MensajeContacto mensaje1 = new MensajeContacto();
        mensaje1.setNombre("Carlos Rodriguez");
        mensaje1.setEmail("carlos.rodriguez@email.com");
        mensaje1.setAsunto("Consulta sobre arriendos en Providencia");
        mensaje1.setMensaje("Hola, estoy buscando un departamento de 2 habitaciones en Providencia. " +
                "Me gustaria saber si tienen opciones disponibles y cual es el proceso para arrendar. " +
                "Mi presupuesto es de aproximadamente $500.000 mensuales. Gracias.");
        mensaje1.setNumeroTelefono("+56912345678");
        mensaje1.setUsuarioId(null); // Usuario no autenticado
        mensaje1.setEstado("PENDIENTE");
        mensaje1.setFechaCreacion(new Date());
        mensajeContactoRepository.save(mensaje1);
        log.info("  - Creado mensaje 1: Consulta arriendos (PENDIENTE)");

        // ============================================================
        // MENSAJE 2: Problema tecnico de usuario autenticado - EN_PROCESO
        // ============================================================
        MensajeContacto mensaje2 = new MensajeContacto();
        mensaje2.setNombre("Juan Perez");
        mensaje2.setEmail("juan.perez@email.com");
        mensaje2.setAsunto("Problema al subir documentos");
        mensaje2.setMensaje("Buenos dias, he intentado subir mi liquidacion de sueldo varias veces " +
                "pero el sistema me muestra un error. El archivo es un PDF de menos de 5MB. " +
                "Podrian ayudarme a resolver este problema? Necesito completar mi solicitud urgente.");
        mensaje2.setNumeroTelefono("+56987654321");
        mensaje2.setUsuarioId(3L); // Usuario autenticado (ARRIENDATARIO)
        mensaje2.setEstado("EN_PROCESO");
        mensaje2.setFechaCreacion(new Date(System.currentTimeMillis() - 86400000)); // Ayer
        mensaje2.setFechaActualizacion(new Date());
        mensajeContactoRepository.save(mensaje2);
        log.info("  - Creado mensaje 2: Problema tecnico (EN_PROCESO)");

        // ============================================================
        // MENSAJE 3: Consulta resuelta - RESUELTO
        // ============================================================
        MensajeContacto mensaje3 = new MensajeContacto();
        mensaje3.setNombre("Maria Lopez");
        mensaje3.setEmail("maria.lopez@duoc.cl");
        mensaje3.setAsunto("Informacion sobre proceso de verificacion");
        mensaje3.setMensaje("Hola, quisiera saber cuanto tiempo toma el proceso de verificacion " +
                "de documentos una vez que los subo a la plataforma. Estoy interesada en " +
                "arrendar un departamento lo antes posible.");
        mensaje3.setNumeroTelefono("+56911223344");
        mensaje3.setUsuarioId(4L); // Usuario autenticado (ARRIENDATARIO)
        mensaje3.setEstado("RESUELTO");
        mensaje3.setFechaCreacion(new Date(System.currentTimeMillis() - 172800000)); // Hace 2 dias
        mensaje3.setFechaActualizacion(new Date(System.currentTimeMillis() - 86400000));
        mensaje3.setRespuesta("Hola Maria, el proceso de verificacion de documentos toma entre " +
                "24 a 48 horas habiles. Una vez aprobados, podras realizar solicitudes de arriendo. " +
                "Si tienes mas consultas, no dudes en escribirnos. Saludos!");
        mensaje3.setRespondidoPor(1L); // Admin ID
        mensajeContactoRepository.save(mensaje3);
        log.info("  - Creado mensaje 3: Consulta verificacion (RESUELTO)");

        // ============================================================
        // MENSAJE 4: Queja de propietario - PENDIENTE
        // ============================================================
        MensajeContacto mensaje4 = new MensajeContacto();
        mensaje4.setNombre("Fabian Gonzalez");
        mensaje4.setEmail("fs.gonzalez@duocuc.cl");
        mensaje4.setAsunto("Solicitud de destacar propiedad");
        mensaje4.setMensaje("Buenas tardes, soy propietario y tengo una propiedad publicada hace " +
                "2 semanas pero no he recibido solicitudes. Me gustaria saber si existe alguna " +
                "opcion para destacar mi publicacion o mejorar su visibilidad en la plataforma.");
        mensaje4.setNumeroTelefono("+56955667788");
        mensaje4.setUsuarioId(2L); // Usuario autenticado (PROPIETARIO)
        mensaje4.setEstado("PENDIENTE");
        mensaje4.setFechaCreacion(new Date());
        mensajeContactoRepository.save(mensaje4);
        log.info("  - Creado mensaje 4: Destacar propiedad (PENDIENTE)");

        // ============================================================
        // MENSAJE 5: Consulta general - PENDIENTE
        // ============================================================
        MensajeContacto mensaje5 = new MensajeContacto();
        mensaje5.setNombre("Ana Martinez");
        mensaje5.setEmail("ana.martinez@gmail.com");
        mensaje5.setAsunto("Disponibilidad en otras comunas");
        mensaje5.setMensaje("Hola, me gustaria saber si tienen propiedades disponibles en La Florida " +
                "o Puente Alto. Busco una casa con patio para mi familia. Somos 4 personas y " +
                "tenemos 2 mascotas. Nuestro presupuesto es de hasta $600.000. Gracias de antemano.");
        mensaje5.setNumeroTelefono(null); // Sin telefono
        mensaje5.setUsuarioId(null); // Usuario no autenticado
        mensaje5.setEstado("PENDIENTE");
        mensaje5.setFechaCreacion(new Date(System.currentTimeMillis() - 3600000)); // Hace 1 hora
        mensajeContactoRepository.save(mensaje5);
        log.info("  - Creado mensaje 5: Consulta comunas (PENDIENTE)");

        // ============================================================
        // MENSAJE 6: Felicitaciones resuelto - RESUELTO
        // ============================================================
        MensajeContacto mensaje6 = new MensajeContacto();
        mensaje6.setNombre("Pedro Ramirez");
        mensaje6.setEmail("pedro.ramirez@email.com");
        mensaje6.setAsunto("Agradecimiento por el servicio");
        mensaje6.setMensaje("Queria agradecerles por la excelente atencion que recibi durante " +
                "el proceso de arriendo. La plataforma es muy facil de usar y el proceso fue " +
                "rapido. Ya estoy instalado en mi nuevo departamento. Muchas gracias!");
        mensaje6.setNumeroTelefono("+56999887766");
        mensaje6.setUsuarioId(5L); // Usuario autenticado (PROPIETARIO)
        mensaje6.setEstado("RESUELTO");
        mensaje6.setFechaCreacion(new Date(System.currentTimeMillis() - 259200000)); // Hace 3 dias
        mensaje6.setFechaActualizacion(new Date(System.currentTimeMillis() - 172800000));
        mensaje6.setRespuesta("Hola Pedro, muchas gracias por tus amables palabras. Nos alegra " +
                "mucho saber que tu experiencia fue positiva. Estamos siempre disponibles si " +
                "necesitas algo mas. Que disfrutes tu nuevo hogar!");
        mensaje6.setRespondidoPor(1L); // Admin ID
        mensajeContactoRepository.save(mensaje6);
        log.info("  - Creado mensaje 6: Agradecimiento (RESUELTO)");

        log.info("Mensajes de contacto creados exitosamente: {} registros",
                mensajeContactoRepository.count());
    }

    /**
     * Imprime resumen de la inicializacion.
     */
    private void printSummary() {
        long total = mensajeContactoRepository.count();
        long pendientes = mensajeContactoRepository.countByEstado("PENDIENTE");
        long enProceso = mensajeContactoRepository.countByEstado("EN_PROCESO");
        long resueltos = mensajeContactoRepository.countByEstado("RESUELTO");

        log.info("========================================================");
        log.info("INICIALIZACION COMPLETADA - CONTACT SERVICE");
        log.info("========================================================");
        log.info("Resumen de datos:");
        log.info("  - Total mensajes: {}", total);
        log.info("  - Pendientes: {}", pendientes);
        log.info("  - En proceso: {}", enProceso);
        log.info("  - Resueltos: {}", resueltos);
        log.info("========================================================");
        log.info("Contact Service listo para recibir peticiones");
        log.info("Puerto: 8085");
        log.info("Swagger UI: http://localhost:8085/swagger-ui/index.html");
        log.info("========================================================");
    }
}