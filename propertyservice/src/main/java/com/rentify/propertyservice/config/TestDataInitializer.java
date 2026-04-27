package com.rentify.propertyservice.config;

import com.rentify.propertyservice.model.*;
import com.rentify.propertyservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Inicializador de Datos de Prueba para Property Service.
 *
 * CARACTERÍSTICAS:
 * - Solo se ejecuta en perfiles "dev" o "test"
 * - Solo se ejecuta si app.init.load-test-data=true
 * - Se ejecuta DESPUÉS de los datos maestros (@Order(10))
 * - Crea 8 propiedades de ejemplo con fotos y categorías
 *
 * PARA HABILITAR:
 * 1. Activar perfil: spring.profiles.active=dev
 * 2. Configurar property: app.init.load-test-data=true
 *
 * PARA DESHABILITAR:
 * - Cambiar a: app.init.load-test-data=false
 * - O usar perfil: spring.profiles.active=prod
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"})
@ConditionalOnProperty(name = "app.init.load-test-data", havingValue = "true", matchIfMissing = false)
public class TestDataInitializer {

    private final PropertyRepository propertyRepository;
    private final FotoRepository fotoRepository;
    private final TipoRepository tipoRepository;
    private final ComunaRepository comunaRepository;
    private final CategoriaRepository categoriaRepository;
    private final RegionRepository regionRepository;

    /**
     * Inicializa propiedades de prueba.
     * Se ejecuta SOLO si no existen propiedades en la BD.
     */
    @Bean
    @Order(10)
    @Transactional
    public CommandLineRunner initTestProperties() {
        return args -> {
            log.info("🔄 Verificando propiedades de prueba...");

            if (propertyRepository.count() > 0) {
                log.info("✅ Propiedades ya existen en la base de datos. Total: {}", propertyRepository.count());
                return;
            }

            log.info("📝 Creando propiedades de prueba...");

            // Obtener datos maestros desde BD
            Tipo departamento = tipoRepository.findByNombre("Departamento")
                    .orElseThrow(() -> new IllegalStateException("Tipo 'Departamento' no encontrado"));
            Tipo casa = tipoRepository.findByNombre("Casa")
                    .orElseThrow(() -> new IllegalStateException("Tipo 'Casa' no encontrado"));

            Comuna santiago = comunaRepository.findByNombre("Santiago")
                    .orElseThrow(() -> new IllegalStateException("Comuna 'Santiago' no encontrada"));
            Comuna maipu = comunaRepository.findByNombre("Maipú")
                    .orElseThrow(() -> new IllegalStateException("Comuna 'Maipú' no encontrada"));
            Comuna colina = comunaRepository.findByNombre("Colina")
                    .orElseThrow(() -> new IllegalStateException("Comuna 'Colina' no encontrada"));
            Comuna estacionCentral = comunaRepository.findByNombre("Estación Central")
                    .orElseThrow(() -> new IllegalStateException("Comuna 'Estación Central' no encontrada"));
            Comuna vitacura = comunaRepository.findByNombre("Vitacura")
                    .orElseThrow(() -> new IllegalStateException("Comuna 'Vitacura' no encontrada"));
            Comuna providencia = comunaRepository.findByNombre("Providencia")
                    .orElseThrow(() -> new IllegalStateException("Comuna 'Providencia' no encontrada"));

            Categoria cercaMetro = categoriaRepository.findByNombre("Cerca del metro")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Cerca del metro' no encontrada"));
            Categoria luminoso = categoriaRepository.findByNombre("Luminoso")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Luminoso' no encontrada"));
            Categoria petFriendly = categoriaRepository.findByNombre("Pet-Friendly")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Pet-Friendly' no encontrada"));
            Categoria conJardin = categoriaRepository.findByNombre("Con jardín")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Con jardín' no encontrada"));
            Categoria conEstacionamiento = categoriaRepository.findByNombre("Con estacionamiento")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Con estacionamiento' no encontrada"));
            Categoria seguridad247 = categoriaRepository.findByNombre("Seguridad 24/7")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Seguridad 24/7' no encontrada"));
            Categoria piscina = categoriaRepository.findByNombre("Piscina")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Piscina' no encontrada"));
            Categoria amoblado = categoriaRepository.findByNombre("Amoblado")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Amoblado' no encontrada"));
            Categoria conBodega = categoriaRepository.findByNombre("Con bodega")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Con bodega' no encontrada"));
            Categoria gimnasio = categoriaRepository.findByNombre("Gimnasio")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Gimnasio' no encontrada"));
            Categoria conTerraza = categoriaRepository.findByNombre("Con terraza")
                    .orElseThrow(() -> new IllegalStateException("Categoría 'Con terraza' no encontrada"));

            // ═══════════════════════════════════════════════════════════════════════
            // PROPIEDAD 1: Departamento Santiago Centro
            // ═══════════════════════════════════════════════════════════════════════
            Property prop1 = Property.builder()
                    .codigo("DP001")
                    .titulo("Depto Santiago centro")
                    .precioMensual(new BigDecimal("550000"))
                    .divisa("CLP")
                    .m2(new BigDecimal("65.50"))
                    .nHabit(2)
                    .nBanos(1)
                    .petFriendly(false)
                    .direccion("Santa Isabel 385, Santiago Centro")
                    .fcreacion(LocalDate.now())
                    .tipo(departamento)
                    .comuna(santiago)
                    .build();
            prop1.addCategoria(cercaMetro);
            prop1.addCategoria(luminoso);
            propertyRepository.save(prop1);

            // Fotos para Propiedad 1
            Foto foto1_1 = Foto.builder()
                    .nombre("sala_estar.jpg")
                    .url("https://www.toppropiedades.cl/imagenes/c1981u6668coc1ea47.jpg")
                    .sortOrder(0)
                    .property(prop1)
                    .build();
            Foto foto1_2 = Foto.builder()
                    .nombre("dormitorio.jpg")
                    .url("https://cf.bstatic.com/xdata/images/hotel/max1024x768/537939601.jpg")
                    .sortOrder(1)
                    .property(prop1)
                    .build();
            Foto foto1_3 = Foto.builder()
                    .nombre("cocina.jpg")
                    .url("https://http2.mlstatic.com/D_NQ_NP_723792-MLC93108865073_092025-O-depto-1-dormitorio-1-bano-santiago-centro.webp")
                    .sortOrder(2)
                    .property(prop1)
                    .build();
            fotoRepository.saveAll(Arrays.asList(foto1_1, foto1_2, foto1_3));

            // ═══════════════════════════════════════════════════════════════════════
            // PROPIEDAD 2: Casa en Maipú
            // ═══════════════════════════════════════════════════════════════════════
            Property prop2 = Property.builder()
                    .codigo("CS001")
                    .titulo("Casa en Maipú")
                    .precioMensual(new BigDecimal("630000"))
                    .divisa("CLP")
                    .m2(new BigDecimal("120.00"))
                    .nHabit(3)
                    .nBanos(2)
                    .petFriendly(true)
                    .direccion("Leonel Calcagni 389, Maipú")
                    .fcreacion(LocalDate.now())
                    .tipo(casa)
                    .comuna(maipu)
                    .build();
            prop2.addCategoria(petFriendly);
            prop2.addCategoria(conJardin);
            prop2.addCategoria(conEstacionamiento);
            propertyRepository.save(prop2);

            // Fotos para Propiedad 2
            Foto foto2_1 = Foto.builder()
                    .nombre("fachada.jpg")
                    .url("https://www.luisduranpropiedades.cl/wp-content/uploads/2022/12/20200729_145338-scaled.jpg")
                    .sortOrder(0)
                    .property(prop2)
                    .build();
            Foto foto2_2 = Foto.builder()
                    .nombre("jardin.jpg")
                    .url("https://http2.mlstatic.com/D_NQ_NP_2X_912378-MLC89933696258_082025-F-casa-en-venta-de-4-dorm-1-bano-en-maipu.webp")
                    .sortOrder(1)
                    .property(prop2)
                    .build();
            fotoRepository.saveAll(Arrays.asList(foto2_1, foto2_2));

            // ═══════════════════════════════════════════════════════════════════════
            // PROPIEDAD 3: Casa Chicureo (Colina)
            // ═══════════════════════════════════════════════════════════════════════
            Property prop3 = Property.builder()
                    .codigo("CS002")
                    .titulo("Casa Chicureo")
                    .precioMensual(new BigDecimal("890000"))
                    .divisa("CLP")
                    .m2(new BigDecimal("180.00"))
                    .nHabit(3)
                    .nBanos(2)
                    .petFriendly(true)
                    .direccion("La Hacienda Chicureo 5")
                    .fcreacion(LocalDate.now())
                    .tipo(casa)
                    .comuna(colina)
                    .build();
            prop3.addCategoria(petFriendly);
            prop3.addCategoria(conJardin);
            prop3.addCategoria(seguridad247);
            prop3.addCategoria(piscina);
            propertyRepository.save(prop3);

            // Fotos para Propiedad 3
            Foto foto3_1 = Foto.builder()
                    .nombre("exterior.jpg")
                    .url("https://http2.mlstatic.com/D_NQ_NP_682250-MLC91589808001_092025-O-arriendo-casa-en-parque-brisas-de-norte-chicureo.webp")
                    .sortOrder(0)
                    .property(prop3)
                    .build();
            Foto foto3_2 = Foto.builder()
                    .nombre("interior.jpg")
                    .url("https://www.ehaus.cl/wp-content/uploads/2023/03/Monica_Molina_Ehouse_PiedraRoja-7-scaled.jpg")
                    .sortOrder(1)
                    .property(prop3)
                    .build();
            fotoRepository.saveAll(Arrays.asList(foto3_1, foto3_2));

            // ═══════════════════════════════════════════════════════════════════════
            // PROPIEDAD 4: Departamento Estación Central
            // ═══════════════════════════════════════════════════════════════════════
            Property prop4 = Property.builder()
                    .codigo("DP002")
                    .titulo("Depto Estacion central")
                    .precioMensual(new BigDecimal("500000"))
                    .divisa("CLP")
                    .m2(new BigDecimal("55.00"))
                    .nHabit(2)
                    .nBanos(1)
                    .petFriendly(false)
                    .direccion("Placilla 65, Estación Central")
                    .fcreacion(LocalDate.now())
                    .tipo(departamento)
                    .comuna(estacionCentral)
                    .build();
            prop4.addCategoria(cercaMetro);
            propertyRepository.save(prop4);

            // Foto para Propiedad 4
            Foto foto4_1 = Foto.builder()
                    .nombre("living.jpg")
                    .url("https://img.resemmedia.com/eyJidWNrZXQiOiJwcmQtbGlmdWxsY29ubmVjdC1iYWNrZW5kLWIyYi1pbWFnZXMiLCJrZXkiOiJwcm9wZXJ0aWVzLzAxOTU5MGRjLTgxMjAtNzc3ZC04OTdkLTAwYWZjZWJlODE1ZS8wMTk1OTBlMC0yZDdkLTcxMDgtODEzMC02MjEwYWVhYzBkZTQuanBnIiwiYnJhbmQiOiJyZXNlbSIsImVkaXRzIjp7InJvdGF0ZSI6bnVsbCwicmVzaXplIjp7IndpZHRoIjo4NDAsImhlaWdodCI6NjMwLCJmaXQiOiJjb3ZlciJ9fX0=")
                    .sortOrder(0)
                    .property(prop4)
                    .build();
            fotoRepository.save(foto4_1);

            // ═══════════════════════════════════════════════════════════════════════
            // PROPIEDAD 5: Moderno Departamento Vitacura
            // ═══════════════════════════════════════════════════════════════════════
            Property prop5 = Property.builder()
                    .codigo("DP003")
                    .titulo("Moderno dpto Vitacura")
                    .precioMensual(new BigDecimal("700000"))
                    .divisa("CLP")
                    .m2(new BigDecimal("85.00"))
                    .nHabit(3)
                    .nBanos(2)
                    .petFriendly(false)
                    .direccion("Vía Aurora 9255, Lo Curro")
                    .fcreacion(LocalDate.now())
                    .tipo(departamento)
                    .comuna(vitacura)
                    .build();
            prop5.addCategoria(amoblado);
            prop5.addCategoria(conEstacionamiento);
            prop5.addCategoria(conBodega);
            prop5.addCategoria(gimnasio);
            propertyRepository.save(prop5);

            // Foto para Propiedad 5
            Foto foto5_1 = Foto.builder()
                    .nombre("vista.jpg")
                    .url("https://http2.mlstatic.com/D_NQ_NP_861463-MLC92409060203_092025-O-moderno-dpto-vitacura-101711.webp")
                    .sortOrder(0)
                    .property(prop5)
                    .build();
            fotoRepository.save(foto5_1);

            // ═══════════════════════════════════════════════════════════════════════
            // PROPIEDAD 6: Acogedor Departamento Santiago
            // ═══════════════════════════════════════════════════════════════════════
            Property prop6 = Property.builder()
                    .codigo("DP004")
                    .titulo("Acogedor depto Santiago")
                    .precioMensual(new BigDecimal("550000"))
                    .divisa("CLP")
                    .m2(new BigDecimal("45.00"))
                    .nHabit(1)
                    .nBanos(1)
                    .petFriendly(false)
                    .direccion("Arturo Prat 595, Santiago")
                    .fcreacion(LocalDate.now())
                    .tipo(departamento)
                    .comuna(santiago)
                    .build();
            prop6.addCategoria(cercaMetro);
            prop6.addCategoria(luminoso);
            propertyRepository.save(prop6);

            // Foto para Propiedad 6
            Foto foto6_1 = Foto.builder()
                    .nombre("ambiente.jpg")
                    .url("https://image.wasi.co/eyJidWNrZXQiOiJzdGF0aWN3Iiwia2V5IjoiaW5tdWVibGVzXC9nMTgyMzM0MjAyMzA4MzEwNTUyMjUuanBnIiwiZWRpdHMiOnsibm9ybWFsaXNlIjp0cnVlLCJyb3RhdGUiOjAsInJlc2l6ZSI6eyJ3aWR0aCI6OTAwLCJoZWlnaHQiOjY3NSwiZml0IjoiY29udGFpbiIsImJhY2tncm91bmQiOnsiciI6MjU1LCJnIjoyNTUsImIiOjI1NSwiYWxwaGEiOjF9fX19")
                    .sortOrder(0)
                    .property(prop6)
                    .build();
            fotoRepository.save(foto6_1);

            // ═══════════════════════════════════════════════════════════════════════
            // PROPIEDAD 7: Casa en Providencia
            // ═══════════════════════════════════════════════════════════════════════
            Property prop7 = Property.builder()
                    .codigo("CS003")
                    .titulo("Casa en Providencia")
                    .precioMensual(new BigDecimal("850000"))
                    .divisa("CLP")
                    .m2(new BigDecimal("150.00"))
                    .nHabit(4)
                    .nBanos(3)
                    .petFriendly(true)
                    .direccion("Av. Providencia 1234")
                    .fcreacion(LocalDate.now())
                    .tipo(casa)
                    .comuna(providencia)
                    .build();
            prop7.addCategoria(petFriendly);
            prop7.addCategoria(conJardin);
            prop7.addCategoria(conTerraza);
            propertyRepository.save(prop7);

            // ═══════════════════════════════════════════════════════════════════════
            // RESUMEN
            // ═══════════════════════════════════════════════════════════════════════
            long totalPropiedades = propertyRepository.count();
            long totalFotos = fotoRepository.count();

            log.info("✅ {} propiedades de prueba creadas exitosamente", totalPropiedades);
            log.info("✅ {} fotos asociadas creadas exitosamente", totalFotos);
            log.info("═══════════════════════════════════════════════════════════");
            log.info("📍 Propiedades disponibles en: http://localhost:8082/api/propiedades");
            log.info("═══════════════════════════════════════════════════════════");
        };
    }
}