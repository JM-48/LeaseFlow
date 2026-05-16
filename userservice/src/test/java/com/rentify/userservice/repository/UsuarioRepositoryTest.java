package com.rentify.userservice.repository;

import com.rentify.userservice.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests de UsuarioRepository")
class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository repository;

    private Usuario usuario1;
    private Usuario usuario2;
    private Usuario usuarioVip;

    @BeforeEach
    void setUp() {
        usuario1 = Usuario.builder()
                .pnombre("Juan")
                .snombre("Carlos")
                .papellido("Pérez")
                .fnacimiento(LocalDate.of(1995, 5, 15))
                .email("juan.perez@email.com")
                .rut("12345678-9")
                .ntelefono("987654321")
                .duocVip(false)
                .clave("password123")
                .puntos(0)
                .codigoRef("ABC123XYZ")
                .fcreacion(LocalDate.now())
                .factualizacion(LocalDate.now())
                .estadoId(1L)
                .rolId(3L)
                .build();
        entityManager.persist(usuario1);

        usuario2 = Usuario.builder()
                .pnombre("María")
                .snombre("Isabel")
                .papellido("González")
                .fnacimiento(LocalDate.of(1990, 8, 20))
                .email("maria.gonzalez@email.com")
                .rut("98765432-1")
                .ntelefono("912345678")
                .duocVip(false)
                .clave("password456")
                .puntos(100)
                .codigoRef("DEF456UVW")
                .fcreacion(LocalDate.now())
                .factualizacion(LocalDate.now())
                .estadoId(1L)
                .rolId(2L)
                .build();
        entityManager.persist(usuario2);

        usuarioVip = Usuario.builder()
                .pnombre("Pedro")
                .snombre("José")
                .papellido("López")
                .fnacimiento(LocalDate.of(1998, 3, 10))
                .email("pedro.lopez@duoc.cl")
                .rut("11223344-5")
                .ntelefono("956789012")
                .duocVip(true)
                .clave("password789")
                .puntos(50)
                .codigoRef("GHI789RST")
                .fcreacion(LocalDate.now())
                .factualizacion(LocalDate.now())
                .estadoId(1L)
                .rolId(3L)
                .build();
        entityManager.persist(usuarioVip);

        entityManager.flush();
    }

    @Test
    @DisplayName("findByEmail - Debería encontrar usuario por email")
    void findByEmail_DeberiaRetornarUsuario() {
        // When
        Optional<Usuario> resultado = repository.findByEmail("juan.perez@email.com");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("juan.perez@email.com");
        assertThat(resultado.get().getPnombre()).isEqualTo("Juan");
    }

    @Test
    @DisplayName("findByEmail - Debería retornar empty cuando no existe")
    void findByEmail_NoExiste_RetornaEmpty() {
        // When
        Optional<Usuario> resultado = repository.findByEmail("noexiste@email.com");

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByRut - Debería encontrar usuario por RUT")
    void findByRut_DeberiaRetornarUsuario() {
        // When
        Optional<Usuario> resultado = repository.findByRut("12345678-9");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getRut()).isEqualTo("12345678-9");
        assertThat(resultado.get().getPnombre()).isEqualTo("Juan");
    }

    @Test
    @DisplayName("findByRut - Debería retornar empty cuando no existe")
    void findByRut_NoExiste_RetornaEmpty() {
        // When
        Optional<Usuario> resultado = repository.findByRut("00000000-0");

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByCodigoRef - Debería encontrar usuario por código de referido")
    void findByCodigoRef_DeberiaRetornarUsuario() {
        // When
        Optional<Usuario> resultado = repository.findByCodigoRef("ABC123XYZ");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCodigoRef()).isEqualTo("ABC123XYZ");
    }

    @Test
    @DisplayName("existsByEmail - Debería retornar true cuando existe")
    void existsByEmail_Existe_RetornaTrue() {
        // When
        boolean existe = repository.existsByEmail("juan.perez@email.com");

        // Then
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - Debería retornar false cuando no existe")
    void existsByEmail_NoExiste_RetornaFalse() {
        // When
        boolean existe = repository.existsByEmail("noexiste@email.com");

        // Then
        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("existsByRut - Debería retornar true cuando existe")
    void existsByRut_Existe_RetornaTrue() {
        // When
        boolean existe = repository.existsByRut("12345678-9");

        // Then
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByRut - Debería retornar false cuando no existe")
    void existsByRut_NoExiste_RetornaFalse() {
        // When
        boolean existe = repository.existsByRut("00000000-0");

        // Then
        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("existsByCodigoRef - Debería retornar true cuando existe")
    void existsByCodigoRef_Existe_RetornaTrue() {
        // When
        boolean existe = repository.existsByCodigoRef("ABC123XYZ");

        // Then
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByCodigoRef - Debería retornar false cuando no existe")
    void existsByCodigoRef_NoExiste_RetornaFalse() {
        // When
        boolean existe = repository.existsByCodigoRef("NOEXISTE");

        // Then
        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("findByRolId - Debería encontrar usuarios por rol")
    void findByRolId_DeberiaRetornarUsuariosDelRol() {
        // When
        List<Usuario> usuarios = repository.findByRolId(3L);

        // Then
        assertThat(usuarios).hasSize(2);
        assertThat(usuarios).extracting(Usuario::getRolId)
                .containsOnly(3L);
    }

    @Test
    @DisplayName("findByEstadoId - Debería encontrar usuarios por estado")
    void findByEstadoId_DeberiaRetornarUsuariosDelEstado() {
        // When
        List<Usuario> usuarios = repository.findByEstadoId(1L);

        // Then
        assertThat(usuarios).hasSize(3);
        assertThat(usuarios).extracting(Usuario::getEstadoId)
                .containsOnly(1L);
    }

    @Test
    @DisplayName("findByDuocVip - Debería encontrar usuarios VIP")
    void findByDuocVip_DeberiaRetornarUsuariosVIP() {
        // When
        List<Usuario> usuariosVip = repository.findByDuocVip(true);

        // Then
        assertThat(usuariosVip).hasSize(1);
        assertThat(usuariosVip.get(0).getDuocVip()).isTrue();
        assertThat(usuariosVip.get(0).getEmail()).contains("@duoc.cl");
    }

    @Test
    @DisplayName("findByDuocVip - Debería encontrar usuarios no VIP")
    void findByDuocVip_DeberiaRetornarUsuariosNoVIP() {
        // When
        List<Usuario> usuariosNoVip = repository.findByDuocVip(false);

        // Then
        assertThat(usuariosNoVip).hasSize(2);
        assertThat(usuariosNoVip).extracting(Usuario::getDuocVip)
                .containsOnly(false);
    }

    @Test
    @DisplayName("findByRolIdAndEstadoId - Debería encontrar usuarios por rol y estado")
    void findByRolIdAndEstadoId_DeberiaRetornarUsuarios() {
        // When
        List<Usuario> usuarios = repository.findByRolIdAndEstadoId(3L, 1L);

        // Then
        assertThat(usuarios).hasSize(2);
        assertThat(usuarios).allMatch(u -> u.getRolId().equals(3L) && u.getEstadoId().equals(1L));
    }

    @Test
    @DisplayName("save - Debería persistir usuario correctamente")
    void save_DeberiaPersistirUsuario() {
        // Given
        Usuario nuevoUsuario = Usuario.builder()
                .pnombre("Ana")
                .snombre("María")
                .papellido("Rojas")
                .fnacimiento(LocalDate.of(1992, 12, 5))
                .email("ana.rojas@email.com")
                .rut("55667788-9")
                .ntelefono("945678901")
                .duocVip(false)
                .clave("password999")
                .puntos(0)
                .codigoRef("JKL012MNO")
                .fcreacion(LocalDate.now())
                .factualizacion(LocalDate.now())
                .estadoId(1L)
                .rolId(3L)
                .build();

        // When
        Usuario saved = repository.save(nuevoUsuario);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("findAll - Debería retornar todos los usuarios")
    void findAll_DeberiaRetornarTodosLosUsuarios() {
        // When
        List<Usuario> usuarios = repository.findAll();

        // Then
        assertThat(usuarios).hasSize(3);
    }

    @Test
    @DisplayName("delete - Debería eliminar usuario correctamente")
    void delete_DeberiaEliminarUsuario() {
        // Given
        Long id = usuario1.getId();

        // When
        repository.delete(usuario1);
        entityManager.flush();

        // Then
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("update - Debería actualizar usuario correctamente")
    void update_DeberiaActualizarUsuario() {
        // Given
        usuario1.setPuntos(200);
        usuario1.setFactualizacion(LocalDate.now());

        // When
        Usuario updated = repository.save(usuario1);

        // Then
        assertThat(updated.getPuntos()).isEqualTo(200);
        assertThat(updated.getId()).isEqualTo(usuario1.getId());
    }
}