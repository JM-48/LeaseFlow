package com.rentify.userservice.repository

import com.rentify.userservice.model.Estado
import org.assertj.core.api.Assertions
import org.assertj.core.api.iterable.ThrowingExtractor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests de EstadoRepository")
internal open class EstadoRepositoryTest {
    @Autowired
    private val entityManager: TestEntityManager? = null

    @Autowired
    private val repository: EstadoRepository? = null

    private var estadoActivo: Estado? = null
    private var estadoInactivo: Estado? = null

    @BeforeEach
    fun setUp() {
        estadoActivo = Estado.builder()
            .nombre("ACTIVO")
            .build()
        entityManager!!.persist<Estado?>(estadoActivo)

        estadoInactivo = Estado.builder()
            .nombre("INACTIVO")
            .build()
        entityManager.persist<Estado?>(estadoInactivo)

        entityManager.flush()
    }

    @Test
    @DisplayName("findByNombre - Debería encontrar estado por nombre")
    fun findByNombre_DeberiaRetornarEstado() {
        // When
        val resultado = repository!!.findByNombre("ACTIVO")

        // Then
        Assertions.assertThat<Estado?>(resultado).isPresent()
        Assertions.assertThat(resultado!!.get().getNombre()).isEqualTo("ACTIVO")
    }

    @Test
    @DisplayName("findByNombre - Debería retornar empty cuando no existe")
    fun findByNombre_NoExiste_RetornaEmpty() {
        // When
        val resultado = repository!!.findByNombre("NO_EXISTE")

        // Then
        Assertions.assertThat<Estado?>(resultado).isEmpty()
    }

    @Test
    @DisplayName("existsByNombre - Debería retornar true cuando existe")
    fun existsByNombre_Existe_RetornaTrue() {
        // When
        val existe = repository!!.existsByNombre("ACTIVO")

        // Then
        Assertions.assertThat(existe).isTrue()
    }

    @Test
    @DisplayName("existsByNombre - Debería retornar false cuando no existe")
    fun existsByNombre_NoExiste_RetornaFalse() {
        // When
        val existe = repository!!.existsByNombre("NO_EXISTE")

        // Then
        Assertions.assertThat(existe).isFalse()
    }

    @Test
    @DisplayName("save - Debería persistir estado correctamente")
    fun save_DeberiaPersistirEstado() {
        // Given
        val nuevoEstado = Estado.builder()
            .nombre("SUSPENDIDO")
            .build()

        // When
        val saved = repository!!.save<Estado?>(nuevoEstado)

        // Then
        Assertions.assertThat(saved.getId()).isNotNull()
        Assertions.assertThat<Estado?>(repository.findById(saved.getId())).isPresent()
    }

    @Test
    @DisplayName("findAll - Debería retornar todos los estados")
    fun findAll_DeberiaRetornarTodosLosEstados() {
        // When
        val estados = repository!!.findAll()

        // Then
        Assertions.assertThat<Estado?>(estados).hasSize(2)
        Assertions.assertThat<Estado?>(estados)
            .extracting<String?, RuntimeException?>(ThrowingExtractor { obj: Estado? -> obj!!.getNombre() })
            .containsExactlyInAnyOrder("ACTIVO", "INACTIVO")
    }

    @Test
    @DisplayName("findById - Debería retornar estado por ID")
    fun findById_DeberiaRetornarEstado() {
        // When
        val resultado = repository!!.findById(estadoActivo!!.getId())

        // Then
        Assertions.assertThat<Estado?>(resultado).isPresent()
        Assertions.assertThat(resultado.get().getNombre()).isEqualTo("ACTIVO")
    }

    @Test
    @DisplayName("delete - Debería eliminar estado correctamente")
    fun delete_DeberiaEliminarEstado() {
        // Given
        val id = estadoActivo!!.getId()

        // When
        repository!!.delete(estadoActivo)
        entityManager!!.flush()

        // Then
        Assertions.assertThat<Estado?>(repository.findById(id)).isEmpty()
    }
}