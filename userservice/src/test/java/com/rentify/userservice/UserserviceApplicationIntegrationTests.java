package com.rentify.userservice;

import com.rentify.userservice.repository.EstadoRepository;
import com.rentify.userservice.repository.RolRepository;
import com.rentify.userservice.repository.UsuarioRepository;
import com.rentify.userservice.service.EstadoService;
import com.rentify.userservice.service.RolService;
import com.rentify.userservice.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Tests de Integración de la Aplicación")
class UserserviceApplicationIntegrationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private UsuarioRepository usuarioRepository;

    @Autowired(required = false)
    private RolRepository rolRepository;

    @Autowired(required = false)
    private EstadoRepository estadoRepository;

    @Autowired(required = false)
    private UsuarioService usuarioService;

    @Autowired(required = false)
    private RolService rolService;

    @Autowired(required = false)
    private EstadoService estadoService;

    @Test
    @DisplayName("El contexto de la aplicación debe cargar correctamente")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("Todos los beans de configuración deben estar disponibles")
    void configurationBeansShouldBeAvailable() {
        // Verificar que los beans de configuración están disponibles
        assertThat(applicationContext.containsBean("modelMapper")).isTrue();
        assertThat(applicationContext.containsBean("webClientBuilder")).isTrue();
        assertThat(applicationContext.containsBean("customOpenAPI")).isTrue();
    }

    @Test
    @DisplayName("Todos los repositorios deben estar disponibles")
    void repositoriesShouldBeAvailable() {
        assertThat(usuarioRepository).isNotNull();
        assertThat(rolRepository).isNotNull();
        assertThat(estadoRepository).isNotNull();
    }

    @Test
    @DisplayName("Todos los servicios deben estar disponibles")
    void servicesShouldBeAvailable() {
        assertThat(usuarioService).isNotNull();
        assertThat(rolService).isNotNull();
        assertThat(estadoService).isNotNull();
    }

    @Test
    @DisplayName("La base de datos H2 debe estar configurada correctamente")
    void databaseShouldBeConfigured() {
        // Verificar que podemos acceder a los repositorios
        assertThat(usuarioRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(rolRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(estadoRepository.count()).isGreaterThanOrEqualTo(0);
    }
}