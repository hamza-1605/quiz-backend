package com.hscodeworks.quiz_backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class HealthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform( get("/health") )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("$.status").value("UP") );
    }
}