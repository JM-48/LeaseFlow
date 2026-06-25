package com.rentify.userservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test unitario aislado del ApiKeyInterceptor.
 * No requiere @SpringBootTest ni @WebMvcTest: monta un MockMvc standalone
 * con un controlador de prueba minimo y el interceptor bajo prueba.
 */
class ApiKeyInterceptorTest {

    private static final String TEST_KEY = "test-key-123";

    private MockMvc mockMvc;

    @RestController
    static class DummyController {
        @GetMapping("/api/dummy")
        public String dummy() {
            return "ok";
        }
    }

    @BeforeEach
    void setUp() {
        ApiKeyInterceptor interceptor = new ApiKeyInterceptor();
        ReflectionTestUtils.setField(interceptor, "expectedClientKey", TEST_KEY);

        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .addInterceptors(interceptor)
                .build();
    }

    @Test
    void sinHeader_deberiaRetornar403() throws Exception {
        mockMvc.perform(get("/api/dummy"))
                .andExpect(status().isForbidden());
    }

    @Test
    void conHeaderIncorrecto_deberiaRetornar403() throws Exception {
        mockMvc.perform(get("/api/dummy")
                        .header("X-App-Client", "valor-incorrecto"))
                .andExpect(status().isForbidden());
    }

    @Test
    void conHeaderCorrecto_deberiaRetornar200() throws Exception {
        mockMvc.perform(get("/api/dummy")
                        .header("X-App-Client", TEST_KEY))
                .andExpect(status().isOk());
    }

    @Test
    void conHeaderVacio_deberiaRetornar403() throws Exception {
        mockMvc.perform(get("/api/dummy")
                        .header("X-App-Client", ""))
                .andExpect(status().isForbidden());
    }
}