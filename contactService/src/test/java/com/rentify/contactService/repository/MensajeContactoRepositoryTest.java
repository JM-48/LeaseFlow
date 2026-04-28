package com.rentify.contactService.repository;

import com.rentify.contactService.model.MensajeContacto;
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
@DisplayName("Tests de MensajeContactoRepository")
class MensajeContactoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MensajeContactoRepository repository;

    private MensajeContacto mensaje1;
    private MensajeContacto mensaje2;

    @BeforeEach
    void setUp() {
        mensaje1 = MensajeContacto.builder()
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .asunto("Consulta sobre departamento disponible")  // ← CAMBIADO
                .mensaje("Quisiera más información sobre el departamento")
                .numeroTelefono("+56912345678")
                .usuarioId(1L)
                .estado("PENDIENTE")
                .fechaCreacion(new Date())
                .build();

        mensaje2 = MensajeContacto.builder()
                .nombre("María González")
                .email("maria@email.com")
                .asunto("Problema con transferencia")  // ← CAMBIADO
                .mensaje("No puedo realizar la transferencia bancaria")  // ← CAMBIADO
                .estado("EN_PROCESO")
                .fechaCreacion(new Date())
                .build();

        entityManager.persist(mensaje1);
        entityManager.persist(mensaje2);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByEmail - Debería encontrar mensajes por email")
    void findByEmail_DeberiaRetornarMensajesDelEmail() {
        // When
        List<MensajeContacto> mensajes = repository.findByEmail("juan@email.com");

        // Then
        assertThat(mensajes).hasSize(1);
        assertThat(mensajes.get(0).getEmail()).isEqualTo("juan@email.com");
        assertThat(mensajes.get(0).getNombre()).isEqualTo("Juan Pérez");
    }

    @Test
    @DisplayName("findByUsuarioId - Debería encontrar mensajes del usuario")
    void findByUsuarioId_DeberiaRetornarMensajesDelUsuario() {
        // When
        List<MensajeContacto> mensajes = repository.findByUsuarioId(1L);

        // Then
        assertThat(mensajes).hasSize(1);
        assertThat(mensajes.get(0).getUsuarioId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByEstado - Debería encontrar mensajes por estado")
    void findByEstado_DeberiaRetornarMensajesPorEstado() {
        // When
        List<MensajeContacto> pendientes = repository.findByEstado("PENDIENTE");

        // Then
        assertThat(pendientes).hasSize(1);
        assertThat(pendientes.get(0).getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("countByEmailAndEstado - Debería contar mensajes por email y estado")
    void countByEmailAndEstado_DeberiaContarCorrectamente() {
        // When
        long count = repository.countByEmailAndEstado("juan@email.com", "PENDIENTE");

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("countByUsuarioIdAndEstado - Debería contar mensajes por usuario y estado")
    void countByUsuarioIdAndEstado_DeberiaContarCorrectamente() {
        // When
        long count = repository.countByUsuarioIdAndEstado(1L, "PENDIENTE");

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("existsByEmailAndEstado - Debería verificar existencia")
    void existsByEmailAndEstado_DeberiaVerificarExistencia() {
        // When
        boolean exists = repository.existsByEmailAndEstado("juan@email.com", "PENDIENTE");
        boolean notExists = repository.existsByEmailAndEstado("juan@email.com", "RESUELTO");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("findMensajesSinResponder - Debería encontrar mensajes sin respuesta")
    void findMensajesSinResponder_DeberiaRetornarMensajesSinRespondidoPor() {
        // When
        List<MensajeContacto> sinResponder = repository.findMensajesSinResponder();

        // Then
        assertThat(sinResponder).hasSize(1);
        assertThat(sinResponder.get(0).getRespondidoPor()).isNull();
    }

    @Test
    @DisplayName("searchByKeyword - Debería buscar por palabra clave en asunto")
    void searchByKeyword_DeberiaBuscarEnAsunto() {
        // When
        List<MensajeContacto> resultados = repository.searchByKeyword("departamento");  // ← CAMBIADO

        // Then
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getAsunto()).containsIgnoringCase("departamento");
    }

    @Test
    @DisplayName("searchByKeyword - Debería buscar por palabra clave en mensaje")
    void searchByKeyword_DeberiaBuscarEnMensaje() {
        // When
        List<MensajeContacto> resultados = repository.searchByKeyword("información");  // ← CAMBIADO

        // Then
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getMensaje()).containsIgnoringCase("información");
    }

    @Test
    @DisplayName("searchByKeyword - Debería buscar en ambos campos")
    void searchByKeyword_DeberiaBuscarEnAmbos() {
        // When - buscar por palabra que aparece en mensaje1 (asunto y mensaje)
        List<MensajeContacto> resultados = repository.searchByKeyword("departamento");

        // Then
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getNombre()).isEqualTo("Juan Pérez");
    }

    @Test
    @DisplayName("save - Debería persistir mensaje correctamente")
    void save_DeberiaPersistirMensaje() {
        // Given
        MensajeContacto nuevo = MensajeContacto.builder()
                .nombre("Pedro López")
                .email("pedro@email.com")
                .asunto("Nueva consulta")
                .mensaje("Este es un mensaje de prueba")
                .estado("PENDIENTE")
                .fechaCreacion(new Date())
                .build();

        // When
        MensajeContacto saved = repository.save(nuevo);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("findAll - Debería retornar todos los mensajes")
    void findAll_DeberiaRetornarTodosLosMensajes() {
        // When
        List<MensajeContacto> todos = repository.findAll();

        // Then
        assertThat(todos).hasSize(2);
    }
}