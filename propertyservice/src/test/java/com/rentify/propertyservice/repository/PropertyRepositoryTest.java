package com.rentify.propertyservice.repository;

import com.rentify.propertyservice.model.Comuna;
import com.rentify.propertyservice.model.Property;
import com.rentify.propertyservice.model.Region;
import com.rentify.propertyservice.model.Tipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de integración para PropertyRepository.
 * Utiliza @DataJpaTest con BD en memoria H2.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // <-- Clave para respetar application-test.properties
@DisplayName("Tests de PropertyRepository")
class PropertyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private TipoRepository tipoRepository;

    @Autowired
    private ComunaRepository comunaRepository;

    @Autowired
    private RegionRepository regionRepository;

    private Property property1;
    private Property property2;
    private Tipo tipoDepartamento;
    private Comuna comunaProvidencia;
    private Region regionMetropolitana;

    @BeforeEach
    void setUp() {
        // Crear Región con nombre único
        regionMetropolitana = Region.builder()
                .nombre("Región Metropolitana_TEST")
                .build();
        entityManager.persist(regionMetropolitana);

        // Crear Comuna con nombre único
        comunaProvidencia = Comuna.builder()
                .nombre("Providencia_TEST")
                .region(regionMetropolitana)
                .build();
        entityManager.persist(comunaProvidencia);

        // Crear Tipo con nombre único
        tipoDepartamento = Tipo.builder()
                .nombre("Departamento_TEST")
                .build();
        entityManager.persist(tipoDepartamento);

        // Crear Propiedades con códigos únicos y agregando propietarioId
        property1 = Property.builder()
                .codigo("DP001_TEST")
                .titulo("Dpto 2D/2B Providencia")
                .precioMensual(BigDecimal.valueOf(650000))
                .divisa("CLP")
                .m2(BigDecimal.valueOf(65.5))
                .nHabit(2)
                .nBanos(2)
                .petFriendly(true)
                .direccion("Av. Providencia 1234")
                .fcreacion(LocalDate.now())
                .tipo(tipoDepartamento)
                .comuna(comunaProvidencia)
                .propietarioId(1L) // <-- AGREGADO
                .build();

        property2 = Property.builder()
                .codigo("DP002_TEST")
                .titulo("Dpto 1D/1B Providencia")
                .precioMensual(BigDecimal.valueOf(450000))
                .divisa("CLP")
                .m2(BigDecimal.valueOf(45.0))
                .nHabit(1)
                .nBanos(1)
                .petFriendly(false)
                .direccion("Av. Providencia 5678")
                .fcreacion(LocalDate.now())
                .tipo(tipoDepartamento)
                .comuna(comunaProvidencia)
                .propietarioId(2L) // <-- AGREGADO
                .build();

        entityManager.persist(property1);
        entityManager.persist(property2);
        entityManager.flush();
    }

    // ==================== Pruebas CRUD ====================

    @Test
    @DisplayName("findById - Debería encontrar propiedad por ID")
    void findById_DeberiaRetornarPropiedad() {
        // When
        var resultado = propertyRepository.findById(property1.getId());

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCodigo()).isEqualTo("DP001_TEST");
        assertThat(resultado.get().getTitulo()).contains("2D/2B");
    }

    @Test
    @DisplayName("findByCodigo - Debería encontrar propiedad por código")
    void findByCodigo_DeberiaRetornarPropiedad() {
        // When
        var resultado = propertyRepository.findByCodigo("DP001_TEST");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(property1.getId());
    }

    @Test
    @DisplayName("save - Debería persistir propiedad correctamente")
    void save_DeberiaPersistirPropiedad() {
        // Given
        Property nueva = Property.builder()
                .codigo("DP999_TEST")
                .titulo("Nueva Propiedad")
                .precioMensual(BigDecimal.valueOf(550000))
                .divisa("CLP")
                .m2(BigDecimal.valueOf(55.0))
                .nHabit(2)
                .nBanos(1)
                .petFriendly(true)
                .direccion("Dirección Nueva")
                .fcreacion(LocalDate.now())
                .tipo(tipoDepartamento)
                .comuna(comunaProvidencia)
                .propietarioId(3L) // <-- AGREGADO
                .build();

        // When
        Property saved = propertyRepository.save(nueva);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(propertyRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("existsByCodigo - Debería verificar existencia por código")
    void existsByCodigo_DeberiaRetornarTrue() {
        // When
        boolean existe = propertyRepository.existsByCodigo("DP001_TEST");

        // Then
        assertThat(existe).isTrue();
        assertThat(propertyRepository.existsByCodigo("NOEXISTE_TEST")).isFalse();
    }

    // ==================== Pruebas de Filtrado ====================

    @Test
    @DisplayName("findByComunaId - Debería encontrar propiedades por comuna")
    void findByComunaId_DeberiaRetornarPropiedades() {
        // When
        List<Property> propiedades = propertyRepository.findByComunaId(comunaProvidencia.getId());

        // Then
        assertThat(propiedades).hasSize(2);
        assertThat(propiedades).extracting(Property::getCodigo).contains("DP001_TEST", "DP002_TEST");
    }

    @Test
    @DisplayName("findByTipoId - Debería encontrar propiedades por tipo")
    void findByTipoId_DeberiaRetornarPropiedades() {
        // When
        List<Property> propiedades = propertyRepository.findByTipoId(tipoDepartamento.getId());

        // Then
        assertThat(propiedades).isNotEmpty();
        assertThat(propiedades).allMatch(p -> p.getTipo().getId().equals(tipoDepartamento.getId()));
    }

    @Test
    @DisplayName("findByPetFriendly - Debería encontrar propiedades pet-friendly")
    void findByPetFriendly_DeberiaRetornarPropiedadesPetFriendly() {
        // When
        List<Property> propiedades = propertyRepository.findByPetFriendly(true);

        // Then
        assertThat(propiedades).hasSize(1);
        assertThat(propiedades.get(0).getCodigo()).isEqualTo("DP001_TEST");
    }

    @Test
    @DisplayName("findByNHabit - Debería encontrar propiedades por habitaciones")
    void findByNHabit_DeberiaRetornarPropiedades() {
        // When
        List<Property> propiedades = propertyRepository.findByNHabit(2);

        // Then
        assertThat(propiedades).hasSize(1);
        assertThat(propiedades.get(0).getCodigo()).isEqualTo("DP001_TEST");
    }

    @Test
    @DisplayName("findByPrecioRange - Debería encontrar propiedades en rango de precio")
    void findByPrecioRange_DeberiaRetornarPropiedades() {
        // When
        List<Property> propiedades = propertyRepository.findByPrecioRange(
                BigDecimal.valueOf(400000),
                BigDecimal.valueOf(700000)
        );

        // Then
        assertThat(propiedades).hasSize(2);
    }

    @Test
    @DisplayName("findByFilters - Debería aplicar múltiples filtros correctamente")
    void findByFilters_DeberiaAplicarFiltrosCombinados() {
        // When
        List<Property> propiedades = propertyRepository.findByFilters(
                comunaProvidencia.getId(),
                tipoDepartamento.getId(),
                BigDecimal.valueOf(600000),  // minPrecio
                BigDecimal.valueOf(700000),  // maxPrecio
                2,                            // nHabit
                2,                            // nBanos
                true                          // petFriendly
        );

        // Then
        assertThat(propiedades).hasSize(1);
        assertThat(propiedades.get(0).getCodigo()).isEqualTo("DP001_TEST");
    }

    @Test
    @DisplayName("findByFilters - Debería retornar vacío si no hay coincidencias")
    void findByFilters_DeberiaRetornarVacio() {
        // When
        List<Property> propiedades = propertyRepository.findByFilters(
                comunaProvidencia.getId(),
                tipoDepartamento.getId(),
                BigDecimal.valueOf(1000000),  // minPrecio muy alto
                BigDecimal.valueOf(2000000),
                null,
                null,
                null
        );

        // Then
        assertThat(propiedades).isEmpty();
    }

    // ==================== Pruebas de Conteo ====================

    @Test
    @DisplayName("countByComunaId - Debería contar propiedades por comuna")
    void countByComunaId_DeberiaRetornarConteo() {
        // When
        long count = propertyRepository.countByComunaId(comunaProvidencia.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countByTipoId - Debería contar propiedades por tipo")
    void countByTipoId_DeberiaRetornarConteo() {
        // When
        long count = propertyRepository.countByTipoId(tipoDepartamento.getId());

        // Then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}