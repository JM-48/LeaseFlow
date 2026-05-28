package com.rentify.documentService.repository;

import com.rentify.documentService.model.TipoDocumento;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Respeta application-test.properties
@DisplayName("Tests de TipoDocumentoRepository")
class TipoDocumentoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Test
    @DisplayName("findByNombre - Debería encontrar tipo de documento por nombre")
    void findByNombre_TipoExiste_RetornaTipo() {
        // Given
        // Verificamos si existe antes de crearlo para evitar duplicados
        Optional<TipoDocumento> existente = tipoDocumentoRepository.findByNombre("DNI_TEST");
        if (existente.isEmpty()) {
            TipoDocumento tipo = TipoDocumento.builder()
                    .nombre("DNI_TEST")
                    .build();
            entityManager.persist(tipo);
            entityManager.flush();
        }

        // When
        Optional<TipoDocumento> found = tipoDocumentoRepository.findByNombre("DNI_TEST");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("DNI_TEST");
    }

    @Test
    @DisplayName("findByNombre - Debería retornar empty si no existe")
    void findByNombre_TipoNoExiste_RetornaEmpty() {
        // When
        Optional<TipoDocumento> found = tipoDocumentoRepository.findByNombre("TIPO_INEXISTENTE_999");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByNombre - Debería verificar existencia correctamente")
    void existsByNombre_DeberiaRetornarTrue() {
        // Given
        Optional<TipoDocumento> existente = tipoDocumentoRepository.findByNombre("LIQUIDACION_TEST");
        if (existente.isEmpty()) {
            TipoDocumento tipo = TipoDocumento.builder()
                    .nombre("LIQUIDACION_TEST")
                    .build();
            entityManager.persist(tipo);
            entityManager.flush();
        }

        // When
        boolean exists = tipoDocumentoRepository.existsByNombre("LIQUIDACION_TEST");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("save - Debería persistir tipo de documento correctamente")
    void save_DeberiaPersistirTipo() {
        // Given
        TipoDocumento tipo = TipoDocumento.builder()
                .nombre("CERTIFICADO_TEST")
                .build();

        // When
        TipoDocumento saved = tipoDocumentoRepository.save(tipo);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(tipoDocumentoRepository.findById(saved.getId())).isPresent();
    }
}