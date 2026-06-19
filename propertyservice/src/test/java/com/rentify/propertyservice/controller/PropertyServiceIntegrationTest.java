package com.rentify.propertyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.propertyservice.dto.ComunaDTO;
import com.rentify.propertyservice.dto.PropertyDTO;
import com.rentify.propertyservice.dto.RegionDTO;
import com.rentify.propertyservice.dto.TipoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Revierte los cambios en la BD después de cada test
@DisplayName("Tests de Integración - PropertyService (Flujo Completo Relacional)")
class PropertyServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Flujo Maestro: Crear Región -> Comuna -> Tipo -> Propiedad -> Búsqueda")
    void flujoCompleto_CrearPropiedadYBuscar() throws Exception {

        // ==========================================
        // PASO 1: Crear una Región
        // ==========================================
        RegionDTO regionDTO = new RegionDTO();
        regionDTO.setNombre("Región Metropolitana");

        MvcResult resultRegion = mockMvc.perform(post("/api/regiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regionDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long regionId = objectMapper.readValue(resultRegion.getResponse().getContentAsString(), RegionDTO.class).getId();

        // ==========================================
        // PASO 2: Crear una Comuna asociada a la Región
        // ==========================================
        ComunaDTO comunaDTO = new ComunaDTO();
        comunaDTO.setNombre("Providencia");
        comunaDTO.setRegionId(regionId);

        MvcResult resultComuna = mockMvc.perform(post("/api/comunas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comunaDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long comunaId = objectMapper.readValue(resultComuna.getResponse().getContentAsString(), ComunaDTO.class).getId();

        // ==========================================
        // PASO 3: Crear un Tipo de Propiedad
        // ==========================================
        TipoDTO tipoDTO = new TipoDTO();
        tipoDTO.setNombre("Departamento");

        MvcResult resultTipo = mockMvc.perform(post("/api/tipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long tipoId = objectMapper.readValue(resultTipo.getResponse().getContentAsString(), TipoDTO.class).getId();

        // ==========================================
        // PASO 4: Crear la Propiedad
        // ==========================================
        PropertyDTO propiedadDTO = PropertyDTO.builder()
                .codigo("DP001")
                .titulo("Hermoso depto en arriendo")
                .precioMensual(new BigDecimal("650000"))
                .divisa("CLP")
                .m2(new BigDecimal("65.5"))
                .nHabit(2)
                .nBanos(2)
                .petFriendly(true)
                .direccion("Av. Providencia 1234")
                .tipoId(tipoId)
                .comunaId(comunaId)
                .propietarioId(99L) // ID simulado del usuario
                .build();

        MvcResult resultPropiedad = mockMvc.perform(post("/api/propiedades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propiedadDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("DP001"))
                .andExpect(jsonPath("$.nHabit").value(2))
                .andReturn();

        Long propiedadId = objectMapper.readValue(resultPropiedad.getResponse().getContentAsString(), PropertyDTO.class).getId();

        // ==========================================
        // PASO 5: Probar el endpoint de Búsqueda con Filtros
        // ==========================================
        mockMvc.perform(get("/api/propiedades/buscar")
                        .param("comunaId", comunaId.toString())
                        .param("petFriendly", "true")
                        .param("minPrecio", "600000")
                        .param("maxPrecio", "700000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(propiedadId))
                .andExpect(jsonPath("$[0].titulo").value("Hermoso depto en arriendo"));
    }

    @Test
    @DisplayName("POST /api/propiedades - Debe fallar (400) si las validaciones no se cumplen")
    void crearPropiedad_ValidacionesFallan_Retorna400() throws Exception {
        // Preparamos un DTO con errores (precio negativo, divisa inválida, código muy largo)
        PropertyDTO propiedadInvalida = PropertyDTO.builder()
                .codigo("CODIGO_DEMASIADO_LARGO_PARA_LA_VALIDACION") // Falla @Size(max=10)
                .titulo("Test")
                .precioMensual(new BigDecimal("-100")) // Falla @DecimalMin
                .divisa("ARS") // Falla @Pattern (Solo CLP, USD, EUR)
                .m2(new BigDecimal("0.5")) // Falla @DecimalMin(1.0)
                .nHabit(-1) // Falla @Min(0)
                .nBanos(30) // Falla @Max(20)
                .build();

        mockMvc.perform(post("/api/propiedades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propiedadInvalida)))
                .andExpect(status().isBadRequest());
    }
}