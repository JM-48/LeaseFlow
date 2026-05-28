package com.rentify.userservice.repository;

import com.rentify.userservice.model.Estado;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Tests de EstadoRepository")
class EstadoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EstadoRepository repository;

    private Estado estadoActivo;
    private Estado estadoInactivo;

    @BeforeEach
    void setUp() {
        estadoActivo = Estado.builder()
                .nombre("ACTIVO")
                .build();
        entityManager.persist(estadoActivo);

        estadoInactivo = Estado.builder()
                .nombre("INACTIVO")
                .build();
        entityManager.persist(estadoInactivo);

        entityManager.flush();
    }

    @Test
    @DisplayName("findByNombre - Debería encontrar estado por nombre")
    void findByNombre_DeberiaRetornarEstado() {
        // When
        Optional<Estado> resultado = repository.findByNombre("ACTIVO");

        // Then
        Assertions.assertThat(resultado).isPresent();
        Assertions.assertThat(resultado.get().getNombre()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("findByNombre - Debería retornar empty cuando no existe")
    void findByNombre_NoExiste_RetornaEmpty() {
        // When
        Optional<Estado> resultado = repository.findByNombre("NO_EXISTE");

        // Then
        Assertions.assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsByNombre - Debería retornar true cuando existe")
    void existsByNombre_Existe_RetornaTrue() {
        // When
        boolean existe = repository.existsByNombre("ACTIVO");

        // Then
        Assertions.assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByNombre - Debería retornar false cuando no existe")
    void existsByNombre_NoExiste_RetornaFalse() {
        // When
        boolean existe = repository.existsByNombre("NO_EXISTE");

        // Then
        Assertions.assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("save - Debería persistir estado correctamente")
    void save_DeberiaPersistirEstado() {
        // Given
        Estado nuevoEstado = Estado.builder()
                .nombre("SUSPENDIDO")
                .build();

        // When
        Estado saved = repository.save(nuevoEstado);

        // Then
        Assertions.assertThat(saved.getId()).isNotNull();
        Assertions.assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("findAll - Debería retornar todos los estados")
    void findAll_DeberiaRetornarTodosLosEstados() {
        // When
        Iterable<Estado> estados = repository.findAll();

        // Then
        Assertions.assertThat(estados).hasSize(2);
        Assertions.assertThat(estados)
                .extracting(Estado::getNombre)
                .containsExactlyInAnyOrder("ACTIVO", "INACTIVO");
    }

    @Test
    @DisplayName("findById - Debería retornar estado por ID")
    void findById_DeberiaRetornarEstado() {
        // When
        Optional<Estado> resultado = repository.findById(estadoActivo.getId());

        // Then
        Assertions.assertThat(resultado).isPresent();
        Assertions.assertThat(resultado.get().getNombre()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("delete - Debería eliminar estado correctamente")
    void delete_DeberiaEliminarEstado() {
        // Given
        Long id = estadoActivo.getId();

        // When
        repository.delete(estadoActivo);
        entityManager.flush();

        // Then
        Assertions.assertThat(repository.findById(id)).isEmpty();
    }
}