package com.hscodeworks.quiz_backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.hscodeworks.quiz_backend.features.category.Category;

class ContentApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void versionEndpointIsPublicAndReturnsTheStoredVersion() throws Exception {
        mockMvc.perform(get("/api/content/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(currentVersion()));
    }

    @Test
    void syncOfAnEmptyDatabaseReturnsEmptyLists() throws Exception {
        mockMvc.perform(get("/api/content/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories.length()").value(0))
                .andExpect(jsonPath("$.questions.length()").value(0));
    }

    @Test
    void syncReturnsEverythingTogetherWithTheCurrentVersion() throws Exception {
        Category space = saveCategory("Space");
        Category islamic = saveCategory("Islamic");
        saveQuestion(space);
        saveQuestion(space);
        saveQuestion(islamic);

        mockMvc.perform(get("/api/content/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(currentVersion()))
                .andExpect(jsonPath("$.categories.length()").value(2))
                .andExpect(jsonPath("$.questions.length()").value(3))
                .andExpect(jsonPath("$.questions[0].categoryId").isNumber())
                .andExpect(jsonPath("$.questions[0].options.length()").value(2));
    }

    @Test
    void syncReflectsANewWriteWithAHigherVersion() throws Exception {
        Category category = saveCategory("Space");
        long before = currentVersion();

        postJson("/api/questions", mcqJson(category.getId(), "New?")).andExpect(status().isCreated());

        mockMvc.perform(get("/api/content/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(before + 1))
                .andExpect(jsonPath("$.questions.length()").value(1));
    }
}