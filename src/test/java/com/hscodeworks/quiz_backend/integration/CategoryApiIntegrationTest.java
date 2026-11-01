package com.hscodeworks.quiz_backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.hscodeworks.quiz_backend.features.category.Category;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionResponse;

class CategoryApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createWithKeyReturns201AndBumpsVersion() throws Exception {
        long before = currentVersion();

        postJson("/api/categories", "{\"name\": \"Space\", \"color\": \"#2E86DE\"}")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Space"));

        assertThat(categoryRepository.count()).isEqualTo(1);
        assertThat(currentVersion()).isEqualTo(before + 1);
    }

    @Test
    void writeWithoutKeyReturns401AndChangesNothing() throws Exception {
        long before = currentVersion();

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Space\"}"))
                .andExpect(status().isUnauthorized());

        assertThat(categoryRepository.count()).isZero();
        assertThat(currentVersion()).isEqualTo(before);
    }

    @Test
    void duplicateNameIgnoringCaseReturns409AndDoesNotBumpVersion() throws Exception {
        saveCategory("Space");
        long before = currentVersion();

        postJson("/api/categories", "{\"name\": \"space\"}")
                .andExpect(status().isConflict());

        assertThat(categoryRepository.count()).isEqualTo(1);
        assertThat(currentVersion()).isEqualTo(before);
    }

    @Test
    void invalidInputReturns400WithFieldErrors() throws Exception {
        long before = currentVersion();

        postJson("/api/categories", "{\"name\": \"\", \"color\": \"red\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.color").exists());

        assertThat(categoryRepository.count()).isZero();
        assertThat(currentVersion()).isEqualTo(before);
    }

    @Test
    void readsArePublic() throws Exception {
        saveCategory("Space");

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Space"));
    }

    @Test
    void unknownCategoryReturns404InProblemFormat() throws Exception {
        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteIsBlockedWhileCategoryHasQuestionsThenAllowedOnceEmpty() throws Exception {
        Category category = saveCategory("Space");
        QuestionResponse question = saveQuestion(category);
        long before = currentVersion();

        deleteWithKey("/api/categories/" + category.getId())
                .andExpect(status().isConflict())
                .andExpect(bodyContains("still has questions"));

        assertThat(categoryRepository.existsById(category.getId())).isTrue();
        assertThat(currentVersion()).isEqualTo(before);

        deleteWithKey("/api/questions/" + question.id()).andExpect(status().isNoContent());
        deleteWithKey("/api/categories/" + category.getId()).andExpect(status().isNoContent());

        assertThat(categoryRepository.count()).isZero();
    }
}