package com.rentify.applicationService.repository;

import com.rentify.applicationService.model.RegistroArriendo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests de integración para RegistroArriendoRepository")
class RegistroArriendoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RegistroArriendoRepository repository;

    private RegistroArriendo registro1;
    private RegistroArriendo registro2;

    @BeforeEach
    void setUp() {
        registro1 = RegistroArriendo.builder()
                .solicitudId(1L)
                .fechaInicio(new Date())
                .montoMensual(500000.0)
                .activo(true)
                .build();

        registro2 = RegistroArriendo.builder()
                .solicitudId(1L)
                .fechaInicio(new Date())
                .fechaFin(new Date())
                .montoMensual(600000.0)
                .activo(false)
                .build();

        entityManager.persist(registro1);
        entityManager.persist(registro2);
        entityManager.flush();
    }

    @Test
    @DisplayName("findBySolicitudId - Debería encontrar registros de la solicitud")
    void findBySolicitudId_DeberiaRetornarRegistrosDeLaSolicitud() {
        // When
        List<RegistroArriendo> registros = repository.findBySolicitudId(1L);

        // Then
        assertThat(registros).hasSize(2);
        assertThat(registros).extracting(RegistroArriendo::getSolicitudId)
                .containsOnly(1L);
    }

    @Test
    @DisplayName("findBySolicitudId - Solicitud sin registros")
    void findBySolicitudId_SolicitudSinRegistros_DeberiaRetornarListaVacia() {
        // When
        List<RegistroArriendo> registros = repository.findBySolicitudId(999L);

        // Then
        assertThat(registros).isEmpty();
    }

    @Test
    @DisplayName("save - Debería persistir registro correctamente")
    void save_DeberiaPersistirRegistro() {
        // Given
        RegistroArriendo nuevoRegistro = RegistroArriendo.builder()
                .solicitudId(2L)
                .fechaInicio(new Date())
                .montoMensual(700000.0)
                .activo(true)
                .build();

        // When
        RegistroArriendo saved = repository.save(nuevoRegistro);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("Filtrar registros activos")
    void filtrarRegistrosActivos() {
        // When
        List<RegistroArriendo> todos = repository.findBySolicitudId(1L);
        List<RegistroArriendo> activos = todos.stream()
                .filter(RegistroArriendo::getActivo)
                .toList();

        // Then
        assertThat(activos).hasSize(1);
        assertThat(activos.get(0).getActivo()).isTrue();
    }
}