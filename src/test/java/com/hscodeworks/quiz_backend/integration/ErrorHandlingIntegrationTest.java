package com.hscodeworks.quiz_backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ErrorHandlingIntegrationTest extends AbstractIntegrationTest {

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/questions/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unknownUrlReturns404InTheSameFormat() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void wrongHttpMethodReturns405() throws Exception {
        deleteWithKey("/api/content/version").andExpect(status().isMethodNotAllowed());
    }

    @Test
    void malformedJsonReturns400WithAFriendlyMessage() throws Exception {
        postJson("/api/categories", "{ not json")
                .andExpect(status().isBadRequest())
                .andExpect(bodyContains("Malformed request body"));
    }

    @Test
    void invalidEnumValueReturns400() throws Exception {
        postJson("/api/questions", """
                {"categoryId": 1, "questionText": "Q?", "type": "ESSAY", "options": []}""")
                .andExpect(status().isBadRequest())
                .andExpect(bodyContains("Malformed request body"));
    }

    @Test
    void unsupportedContentTypeReturns415() throws Exception {
        mockMvc.perform(post("/api/categories")
                .header(KEY_HEADER, API_KEY)
                .contentType(MediaType.TEXT_PLAIN)
                .content("hello"))
                .andExpect(status().isUnsupportedMediaType());
    }
}