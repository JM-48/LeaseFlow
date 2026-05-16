package com.rentify.userservice.repository;

import com.rentify.userservice.model.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests de RolRepository")
class RolRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RolRepository repository;

    private Rol rolAdmin;
    private Rol rolPropietario;
    private Rol rolArriendatario;

    @BeforeEach
    void setUp() {
        rolAdmin = Rol.builder()
                .nombre("ADMIN")
                .build();
        entityManager.persist(rolAdmin);

        rolPropietario = Rol.builder()
                .nombre("PROPIETARIO")
                .build();
        entityManager.persist(rolPropietario);

        rolArriendatario = Rol.builder()
                .nombre("ARRIENDATARIO")
                .build();
        entityManager.persist(rolArriendatario);

        entityManager.flush();
    }

    @Test
    @DisplayName("findByNombre - Debería encontrar rol por nombre")
    void findByNombre_DeberiaRetornarRol() {
        // When
        Optional<Rol> resultado = repository.findByNombre("ADMIN");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("findByNombre - Debería retornar empty cuando no existe")
    void findByNombre_NoExiste_RetornaEmpty() {
        // When
        Optional<Rol> resultado = repository.findByNombre("NO_EXISTE");

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsByNombre - Debería retornar true cuando existe")
    void existsByNombre_Existe_RetornaTrue() {
        // When
        boolean existe = repository.existsByNombre("PROPIETARIO");

        // Then
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByNombre - Debería retornar false cuando no existe")
    void existsByNombre_NoExiste_RetornaFalse() {
        // When
        boolean existe = repository.existsByNombre("NO_EXISTE");

        // Then
        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("save - Debería persistir rol correctamente")
    void save_DeberiaPersistirRol() {
        // Given
        Rol nuevoRol = Rol.builder()
                .nombre("NUEVO_ROL")
                .build();

        // When
        Rol saved = repository.save(nuevoRol);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("findAll - Debería retornar todos los roles")
    void findAll_DeberiaRetornarTodosLosRoles() {
        // When
        List<Rol> roles = repository.findAll();

        // Then
        assertThat(roles).hasSize(3);
        assertThat(roles).extracting(Rol::getNombre)
                .containsExactlyInAnyOrder("ADMIN", "PROPIETARIO", "ARRIENDATARIO");
    }

    @Test
    @DisplayName("findById - Debería retornar rol por ID")
    void findById_DeberiaRetornarRol() {
        // When
        Optional<Rol> resultado = repository.findById(rolAdmin.getId());

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("delete - Debería eliminar rol correctamente")
    void delete_DeberiaEliminarRol() {
        // Given
        Long id = rolAdmin.getId();

        // When
        repository.delete(rolAdmin);
        entityManager.flush();

        // Then
        assertThat(repository.findById(id)).isEmpty();
    }
}