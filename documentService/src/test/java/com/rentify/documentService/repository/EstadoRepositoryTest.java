package com.rentify.documentService.repository;

import com.rentify.documentService.model.Estado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests de EstadoRepository")
class EstadoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EstadoRepository estadoRepository;

    @Test
    @DisplayName("findByNombre - Debería encontrar estado por nombre")
    void findByNombre_EstadoExiste_RetornaEstado() {
        // Given
        Estado estado = Estado.builder()
                .nombre("PENDIENTE")
                .build();
        entityManager.persist(estado);
        entityManager.flush();

        // When
        Optional<Estado> found = estadoRepository.findByNombre("PENDIENTE");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("findByNombre - Debería retornar empty si no existe")
    void findByNombre_EstadoNoExiste_RetornaEmpty() {
        // When
        Optional<Estado> found = estadoRepository.findByNombre("NO_EXISTE");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByNombre - Debería verificar existencia correctamente")
    void existsByNombre_DeberiaRetornarTrue() {
        // Given
        Estado estado = Estado.builder()
                .nombre("ACEPTADO")
                .build();
        entityManager.persist(estado);
        entityManager.flush();

        // When
        boolean exists = estadoRepository.existsByNombre("ACEPTADO");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("save - Debería persistir estado correctamente")
    void save_DeberiaPersistirEstado() {
        // Given
        Estado estado = Estado.builder()
                .nombre("EN_REVISION")
                .build();

        // When
        Estado saved = estadoRepository.save(estado);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(estadoRepository.findById(saved.getId())).isPresent();
    }
}