package com.rentify.reviewService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ReviewServiceApplicationTests {

    @Test
    void contextLoads() {
        // Si este test pasa, significa que Spring levanta correctamente
        // usando tu configuración de H2 en memoria.
    }

}