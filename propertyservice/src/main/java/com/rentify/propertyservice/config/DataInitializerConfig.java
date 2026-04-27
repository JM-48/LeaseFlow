package com.rentify.propertyservice.config;

import com.rentify.propertyservice.model.Categoria;
import com.rentify.propertyservice.model.Comuna;
import com.rentify.propertyservice.model.Region;
import com.rentify.propertyservice.model.Tipo;
import com.rentify.propertyservice.repository.CategoriaRepository;
import com.rentify.propertyservice.repository.ComunaRepository;
import com.rentify.propertyservice.repository.RegionRepository;
import com.rentify.propertyservice.repository.TipoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración para inicialización automática de datos maestros.
 * Puebla las tablas: region, comuna, tipo, categoria al iniciar la aplicación.
 *
 * Orden de ejecución:
 * 1. Regiones (no depende de nadie)
 * 2. Comunas (depende de Regiones)
 * 3. Tipos de Propiedad (independiente)
 * 4. Categorías (independiente)
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializerConfig {

    private final RegionRepository regionRepository;
    private final ComunaRepository comunaRepository;
    private final TipoRepository tipoRepository;
    private final CategoriaRepository categoriaRepository;

    /**
     * PASO 1: Inicializar Regiones de Chile
     */
    @Bean
    @Order(1)
    public CommandLineRunner initRegiones() {
        return args -> {
            log.info("🔄 Verificando regiones...");

            if (regionRepository.count() > 0) {
                log.info("✅ Regiones ya existen en la base de datos. Total: {}", regionRepository.count());
                return;
            }

            log.info("📝 Creando regiones de Chile...");

            List<Region> regiones = Arrays.asList(
                    Region.builder().nombre("Región Metropolitana").build(),
                    Region.builder().nombre("Región de Valparaíso").build(),
                    Region.builder().nombre("Región del Biobío").build(),
                    Region.builder().nombre("Región de La Araucanía").build(),
                    Region.builder().nombre("Región de Los Lagos").build(),
                    Region.builder().nombre("Región de Antofagasta").build(),
                    Region.builder().nombre("Región de Coquimbo").build(),
                    Region.builder().nombre("Región del Maule").build(),
                    Region.builder().nombre("Región de Tarapacá").build(),
                    Region.builder().nombre("Región de Atacama").build(),
                    Region.builder().nombre("Región de Aysén").build(),
                    Region.builder().nombre("Región de Magallanes").build(),
                    Region.builder().nombre("Región de Arica y Parinacota").build()
            );

            regionRepository.saveAll(regiones);
            log.info("✅ {} regiones creadas exitosamente", regiones.size());
        };
    }

    /**
     * PASO 2: Inicializar Comunas (depende de Regiones)
     */
    @Bean
    @Order(2)
    @Transactional
    public CommandLineRunner initComunas() {
        return args -> {
            log.info("🔄 Verificando comunas...");

            if (comunaRepository.count() > 0) {
                log.info("✅ Comunas ya existen en la base de datos. Total: {}", comunaRepository.count());
                return;
            }

            log.info("📝 Creando comunas...");

            // Obtener regiones desde BD
            Region rm = regionRepository.findByNombre("Región Metropolitana")
                    .orElseThrow(() -> new IllegalStateException("Región Metropolitana no encontrada"));
            Region valparaiso = regionRepository.findByNombre("Región de Valparaíso")
                    .orElseThrow(() -> new IllegalStateException("Región de Valparaíso no encontrada"));
            Region biobio = regionRepository.findByNombre("Región del Biobío")
                    .orElseThrow(() -> new IllegalStateException("Región del Biobío no encontrada"));

            // Comunas de Región Metropolitana
            List<Comuna> comunasRM = Arrays.asList(
                    Comuna.builder().nombre("Santiago").region(rm).build(),
                    Comuna.builder().nombre("Providencia").region(rm).build(),
                    Comuna.builder().nombre("Las Condes").region(rm).build(),
                    Comuna.builder().nombre("Ñuñoa").region(rm).build(),
                    Comuna.builder().nombre("La Florida").region(rm).build(),
                    Comuna.builder().nombre("Maipú").region(rm).build(),
                    Comuna.builder().nombre("Puente Alto").region(rm).build(),
                    Comuna.builder().nombre("San Miguel").region(rm).build(),
                    Comuna.builder().nombre("La Reina").region(rm).build(),
                    Comuna.builder().nombre("Peñalolén").region(rm).build(),
                    Comuna.builder().nombre("Macul").region(rm).build(),
                    Comuna.builder().nombre("Estación Central").region(rm).build(),
                    Comuna.builder().nombre("Recoleta").region(rm).build(),
                    Comuna.builder().nombre("Independencia").region(rm).build(),
                    Comuna.builder().nombre("Conchalí").region(rm).build(),
                    Comuna.builder().nombre("Quilicura").region(rm).build(),
                    Comuna.builder().nombre("Renca").region(rm).build(),
                    Comuna.builder().nombre("Quinta Normal").region(rm).build(),
                    Comuna.builder().nombre("Cerro Navia").region(rm).build(),
                    Comuna.builder().nombre("Lo Prado").region(rm).build(),
                    Comuna.builder().nombre("Pudahuel").region(rm).build(),
                    Comuna.builder().nombre("Cerrillos").region(rm).build(),
                    Comuna.builder().nombre("Padre Hurtado").region(rm).build(),
                    Comuna.builder().nombre("San Bernardo").region(rm).build(),
                    Comuna.builder().nombre("Buin").region(rm).build(),
                    Comuna.builder().nombre("Paine").region(rm).build(),
                    Comuna.builder().nombre("Talagante").region(rm).build(),
                    Comuna.builder().nombre("Peñaflor").region(rm).build(),
                    Comuna.builder().nombre("Melipilla").region(rm).build(),
                    Comuna.builder().nombre("Curacaví").region(rm).build(),
                    Comuna.builder().nombre("María Pinto").region(rm).build(),
                    Comuna.builder().nombre("San Pedro").region(rm).build(),
                    Comuna.builder().nombre("Alhué").region(rm).build(),
                    Comuna.builder().nombre("Colina").region(rm).build(),
                    Comuna.builder().nombre("Lampa").region(rm).build(),
                    Comuna.builder().nombre("Tiltil").region(rm).build(),
                    Comuna.builder().nombre("Pirque").region(rm).build(),
                    Comuna.builder().nombre("San José de Maipo").region(rm).build(),
                    Comuna.builder().nombre("Vitacura").region(rm).build(),
                    Comuna.builder().nombre("Lo Barnechea").region(rm).build(),
                    Comuna.builder().nombre("Huechuraba").region(rm).build()
            );

            // Comunas de Valparaíso
            List<Comuna> comunasValpo = Arrays.asList(
                    Comuna.builder().nombre("Valparaíso").region(valparaiso).build(),
                    Comuna.builder().nombre("Viña del Mar").region(valparaiso).build(),
                    Comuna.builder().nombre("Concón").region(valparaiso).build(),
                    Comuna.builder().nombre("Quilpué").region(valparaiso).build(),
                    Comuna.builder().nombre("Villa Alemana").region(valparaiso).build(),
                    Comuna.builder().nombre("Casablanca").region(valparaiso).build(),
                    Comuna.builder().nombre("Quillota").region(valparaiso).build(),
                    Comuna.builder().nombre("La Calera").region(valparaiso).build(),
                    Comuna.builder().nombre("San Antonio").region(valparaiso).build()
            );

            // Comunas de Biobío
            List<Comuna> comunasBiobio = Arrays.asList(
                    Comuna.builder().nombre("Concepción").region(biobio).build(),
                    Comuna.builder().nombre("Talcahuano").region(biobio).build(),
                    Comuna.builder().nombre("Chillán").region(biobio).build(),
                    Comuna.builder().nombre("Los Ángeles").region(biobio).build(),
                    Comuna.builder().nombre("Coronel").region(biobio).build(),
                    Comuna.builder().nombre("San Pedro de la Paz").region(biobio).build(),
                    Comuna.builder().nombre("Tomé").region(biobio).build(),
                    Comuna.builder().nombre("Hualpén").region(biobio).build()
            );

            // Guardar todas las comunas
            comunaRepository.saveAll(comunasRM);
            comunaRepository.saveAll(comunasValpo);
            comunaRepository.saveAll(comunasBiobio);

            long totalComunas = comunasRM.size() + comunasValpo.size() + comunasBiobio.size();
            log.info("✅ {} comunas creadas exitosamente", totalComunas);
            log.info("   - Región Metropolitana: {} comunas", comunasRM.size());
            log.info("   - Región de Valparaíso: {} comunas", comunasValpo.size());
            log.info("   - Región del Biobío: {} comunas", comunasBiobio.size());
        };
    }

    /**
     * PASO 3: Inicializar Tipos de Propiedad
     */
    @Bean
    @Order(3)
    public CommandLineRunner initTipos() {
        return args -> {
            log.info("🔄 Verificando tipos de propiedad...");

            if (tipoRepository.count() > 0) {
                log.info("✅ Tipos de propiedad ya existen en la base de datos. Total: {}", tipoRepository.count());
                return;
            }

            log.info("📝 Creando tipos de propiedad...");

            List<Tipo> tipos = Arrays.asList(
                    Tipo.builder().nombre("Departamento").build(),
                    Tipo.builder().nombre("Casa").build(),
                    Tipo.builder().nombre("Oficina").build(),
                    Tipo.builder().nombre("Local Comercial").build(),
                    Tipo.builder().nombre("Bodega").build(),
                    Tipo.builder().nombre("Parcela").build(),
                    Tipo.builder().nombre("Sitio").build(),
                    Tipo.builder().nombre("Estacionamiento").build()
            );

            tipoRepository.saveAll(tipos);
            log.info("✅ {} tipos de propiedad creados exitosamente", tipos.size());
        };
    }

    /**
     * PASO 4: Inicializar Categorías
     */
    @Bean
    @Order(4)
    public CommandLineRunner initCategorias() {
        return args -> {
            log.info("🔄 Verificando categorías...");

            if (categoriaRepository.count() > 0) {
                log.info("✅ Categorías ya existen en la base de datos. Total: {}", categoriaRepository.count());
                return;
            }

            log.info("📝 Creando categorías...");

            List<Categoria> categorias = Arrays.asList(
                    Categoria.builder().nombre("Amoblado").build(),
                    Categoria.builder().nombre("Pet-Friendly").build(),
                    Categoria.builder().nombre("Con estacionamiento").build(),
                    Categoria.builder().nombre("Con bodega").build(),
                    Categoria.builder().nombre("Luminoso").build(),
                    Categoria.builder().nombre("Con terraza").build(),
                    Categoria.builder().nombre("Con jardín").build(),
                    Categoria.builder().nombre("Cerca del metro").build(),
                    Categoria.builder().nombre("Seguridad 24/7").build(),
                    Categoria.builder().nombre("Piscina").build(),
                    Categoria.builder().nombre("Gimnasio").build(),
                    Categoria.builder().nombre("Quincho").build(),
                    Categoria.builder().nombre("Logia").build(),
                    Categoria.builder().nombre("Walking closet").build(),
                    Categoria.builder().nombre("Smart home").build()
            );

            categoriaRepository.saveAll(categorias);
            log.info("✅ {} categorías creadas exitosamente", categorias.size());
        };
    }

    /**
     * PASO 5: Resumen de inicialización
     */
    @Bean
    @Order(5)
    public CommandLineRunner printInitializationSummary() {
        return args -> {
            log.info("═══════════════════════════════════════════════════════════");
            log.info("🎉 INICIALIZACIÓN DE PROPERTY SERVICE COMPLETADA");
            log.info("═══════════════════════════════════════════════════════════");
            log.info("📊 RESUMEN DE DATOS MAESTROS:");
            log.info("   ✅ Regiones:    {} registros", regionRepository.count());
            log.info("   ✅ Comunas:     {} registros", comunaRepository.count());
            log.info("   ✅ Tipos:       {} registros", tipoRepository.count());
            log.info("   ✅ Categorías:  {} registros", categoriaRepository.count());
            log.info("═══════════════════════════════════════════════════════════");
            log.info("🚀 Property Service listo para recibir propiedades");
            log.info("📍 Swagger UI: http://localhost:8082/swagger-ui/index.html");
            log.info("═══════════════════════════════════════════════════════════");
        };
    }
}