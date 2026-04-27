package com.rentify.reviewService.repository;

import com.rentify.reviewService.model.Review;
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

/**
 * Tests de integración para ReviewRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests de ReviewRepository")
class ReviewRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository repository;

    private Review review1;
    private Review review2;

    @BeforeEach
    void setUp() {
        review1 = Review.builder()
                .usuarioId(1L)
                .propiedadId(1L)
                .puntaje(8)
                .comentario("Excelente propiedad")
                .tipoResenaId(1L)
                .fechaResena(new Date())
                .estado("ACTIVA")
                .build();

        review2 = Review.builder()
                .usuarioId(2L)
                .propiedadId(1L)
                .puntaje(9)
                .comentario("Muy buena ubicación")
                .tipoResenaId(1L)
                .fechaResena(new Date())
                .estado("ACTIVA")
                .build();

        entityManager.persist(review1);
        entityManager.persist(review2);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByUsuarioId - Debería encontrar reseñas del usuario")
    void findByUsuarioId_DeberiaRetornarResenasDelUsuario() {
        // When
        List<Review> reviews = repository.findByUsuarioId(1L);

        // Then
        assertThat(reviews).hasSize(1);
        assertThat(reviews).extracting(Review::getUsuarioId).containsOnly(1L);
    }

    @Test
    @DisplayName("findByPropiedadId - Debería encontrar reseñas de la propiedad")
    void findByPropiedadId_DeberiaRetornarResenasDeLaPropiedad() {
        // When
        List<Review> reviews = repository.findByPropiedadId(1L);

        // Then
        assertThat(reviews).hasSize(2);
        assertThat(reviews).extracting(Review::getPropiedadId).containsOnly(1L);
    }

    @Test
    @DisplayName("existsByUsuarioIdAndPropiedadId - Debería retornar true cuando existe")
    void existsByUsuarioIdAndPropiedadId_CuandoExiste_RetornaTrue() {
        // When
        boolean exists = repository.existsByUsuarioIdAndPropiedadId(1L, 1L);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsuarioIdAndPropiedadId - Debería retornar false cuando no existe")
    void existsByUsuarioIdAndPropiedadId_CuandoNoExiste_RetornaFalse() {
        // When
        boolean exists = repository.existsByUsuarioIdAndPropiedadId(999L, 999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("calcularPromedioPuntajePorPropiedad - Debería calcular promedio correctamente")
    void calcularPromedioPuntajePorPropiedad_DeberiaCalcularPromedio() {
        // When
        Double promedio = repository.calcularPromedioPuntajePorPropiedad(1L);

        // Then
        assertThat(promedio).isNotNull();
        assertThat(promedio).isEqualTo(8.5);
    }

    @Test
    @DisplayName("save - Debería persistir reseña correctamente")
    void save_DeberiaPersistirResena() {
        // Given
        Review nueva = Review.builder()
                .usuarioId(3L)
                .propiedadId(2L)
                .puntaje(7)
                .comentario("Buena propiedad")
                .tipoResenaId(1L)
                .fechaResena(new Date())
                .estado("ACTIVA")
                .build();

        // When
        Review saved = repository.save(nueva);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("findByEstado - Debería encontrar reseñas por estado")
    void findByEstado_DeberiaRetornarResenasPorEstado() {
        // When
        List<Review> reviews = repository.findByEstado("ACTIVA");

        // Then
        assertThat(reviews).hasSize(2);
        assertThat(reviews).extracting(Review::getEstado).containsOnly("ACTIVA");
    }
}