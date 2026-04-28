package com.rentify.applicationService.repository;

import com.rentify.applicationService.model.SolicitudArriendo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SolicitudArriendoRepositoryTest {

    @Autowired
    private SolicitudArriendoRepository repository;

    @Test
    void guardarSolicitud_ok() {
        SolicitudArriendo sol = SolicitudArriendo.builder()
                .usuarioId(1L)
                .propiedadId(10L)
                .estado("PENDIENTE")
                .fechaSolicitud(new Date())
                .build();

        SolicitudArriendo saved = repository.save(sol);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getUsuarioId());
    }

    @Test
    void buscarPorUsuario() {
        SolicitudArriendo sol = SolicitudArriendo.builder()
                .usuarioId(7L)
                .propiedadId(99L)
                .estado("PENDIENTE")
                .fechaSolicitud(new Date())
                .build();

        repository.save(sol);

        assertEquals(1, repository.findByUsuarioId(7L).size());
    }

    @Test
    void countByUsuarioIdAndEstado_ok() {
        SolicitudArriendo sol = SolicitudArriendo.builder()
                .usuarioId(5L)
                .propiedadId(88L)
                .estado("PENDIENTE")
                .fechaSolicitud(new Date())
                .build();

        repository.save(sol);

        long count = repository.countByUsuarioIdAndEstado(5L, "PENDIENTE");

        assertEquals(1L, count);
    }
}
