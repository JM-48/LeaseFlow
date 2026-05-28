package com.rentify.documentService.repository;

import com.rentify.documentService.model.Estado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // <-- Clave para respetar application-test.properties
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
        // Verificamos si existe antes de crearlo para evitar error de duplicados si la BD viene precargada
        Optional<Estado> existente = estadoRepository.findByNombre("PENDIENTE_TEST");
        if (existente.isEmpty()) {
            Estado estado = Estado.builder()
                    .nombre("PENDIENTE_TEST") // Usamos nombres únicos para el test
                    .build();
            entityManager.persist(estado);
            entityManager.flush();
        }

        // When
        Optional<Estado> found = estadoRepository.findByNombre("PENDIENTE_TEST");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("PENDIENTE_TEST");
    }

    @Test
    @DisplayName("findByNombre - Debería retornar empty si no existe")
    void findByNombre_EstadoNoExiste_RetornaEmpty() {
        // When
        Optional<Estado> found = estadoRepository.findByNombre("ESTADO_INEXISTENTE_999");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByNombre - Debería verificar existencia correctamente")
    void existsByNombre_DeberiaRetornarTrue() {
        // Given
        Optional<Estado> existente = estadoRepository.findByNombre("ACEPTADO_TEST");
        if (existente.isEmpty()) {
            Estado estado = Estado.builder()
                    .nombre("ACEPTADO_TEST")
                    .build();
            entityManager.persist(estado);
            entityManager.flush();
        }

        // When
        boolean exists = estadoRepository.existsByNombre("ACEPTADO_TEST");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("save - Debería persistir estado correctamente")
    void save_DeberiaPersistirEstado() {
        // Given
        Estado estado = Estado.builder()
                .nombre("EN_REVISION_TEST")
                .build();

        // When
        Estado saved = estadoRepository.save(estado);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(estadoRepository.findById(saved.getId())).isPresent();
    }
}