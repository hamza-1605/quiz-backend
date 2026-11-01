package com.hscodeworks.quiz_backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.hscodeworks.quiz_backend.features.category.Category;
import com.hscodeworks.quiz_backend.features.image.StoredImage;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionResponse;

class QuestionApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createMultipleChoiceQuestionStoresQuestionAndFourOptions() throws Exception {
        Category category = saveCategory("Space");
        long before = currentVersion();

        postJson("/api/questions", mcqJson(category.getId(), "Which planet is red?"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.options.length()").value(4))
                .andExpect(jsonPath("$.options[0].id").isNumber());

        assertThat(questionRepository.count()).isEqualTo(1);
        assertThat(optionRowCount()).isEqualTo(4);
        assertThat(currentVersion()).isEqualTo(before + 1);
    }

    @Test
    void ruleViolationsReturn400AndSaveNothing() throws Exception {
        Category category = saveCategory("Space");
        long before = currentVersion();

        // true/false with three options
        postJson("/api/questions", """
                {"categoryId": %d, "questionText": "Q?", "type": "TRUE_FALSE",
                 "options": [{"text": "True", "correct": true}, {"text": "False", "correct": false},
                             {"text": "Maybe", "correct": false}]}
                """.formatted(category.getId()))
                .andExpect(status().isBadRequest());

        // two correct answers
        postJson("/api/questions", """
                {"categoryId": %d, "questionText": "Q?", "type": "TRUE_FALSE",
                 "options": [{"text": "True", "correct": true}, {"text": "False", "correct": true}]}
                """.formatted(category.getId()))
                .andExpect(status().isBadRequest());

        assertThat(questionRepository.count()).isZero();
        assertThat(optionRowCount()).isZero();
        assertThat(currentVersion()).isEqualTo(before);
    }

    @Test
    void unknownCategoryReturns404AndSavesNothing() throws Exception {
        long before = currentVersion();

        postJson("/api/questions", mcqJson(999, "Q?")).andExpect(status().isNotFound());

        assertThat(questionRepository.count()).isZero();
        assertThat(currentVersion()).isEqualTo(before);
    }

    @Test
    void updateReplacesTheOptionsInTheDatabase() throws Exception {
        Category category = saveCategory("Space");
        QuestionResponse created = saveQuestion(category); // 2 options
        assertThat(optionRowCount()).isEqualTo(2);
        long before = currentVersion();

        putJson("/api/questions/" + created.id(), mcqJson(category.getId(), "Updated?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText").value("Updated?"))
                .andExpect(jsonPath("$.options.length()").value(4));

        assertThat(optionRowCount()).isEqualTo(4); // the old 2 are gone, not 6
        assertThat(currentVersion()).isEqualTo(before + 1);
    }

    @Test
    void deletingAQuestionRemovesItsOptions() throws Exception {
        Category category = saveCategory("Space");
        QuestionResponse created = saveQuestion(category);
        assertThat(optionRowCount()).isEqualTo(2);

        deleteWithKey("/api/questions/" + created.id()).andExpect(status().isNoContent());

        assertThat(questionRepository.count()).isZero();
        assertThat(optionRowCount()).isZero();
    }

    @Test
    void questionsOfACategoryAreListedPublicly() throws Exception {
        Category category = saveCategory("Space");
        saveQuestion(category);
        saveQuestion(category);

        mockMvc.perform(get("/api/categories/" + category.getId() + "/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void uploadingAndReplacingAnImageSchedulesCleanupOfTheOldOne() throws Exception {
        Category category = saveCategory("Space");
        QuestionResponse question = saveQuestion(category);
        when(imageStorageService.upload(any()))
                .thenReturn(new StoredImage("https://img.test/one.png", "public-1"))
                .thenReturn(new StoredImage("https://img.test/two.png", "public-2"));
        long before = currentVersion();
        String url = "/api/questions/" + question.id() + "/image";

        uploadImage(url)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://img.test/one.png"));
        verify(imageStorageService).deleteIfRolledBack("public-1");
        assertThat(currentVersion()).isEqualTo(before + 1);

        uploadImage(url)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://img.test/two.png"));
        verify(imageStorageService).deleteAfterCommit("public-1");
        assertThat(currentVersion()).isEqualTo(before + 2);
    }

    @Test
    void removingAnImageClearsItAndSchedulesCleanup() throws Exception {
        Category category = saveCategory("Space");
        QuestionResponse question = saveQuestion(category);
        when(imageStorageService.upload(any())).thenReturn(new StoredImage("https://img.test/one.png", "public-1"));
        String url = "/api/questions/" + question.id() + "/image";
        uploadImage(url).andExpect(status().isOk());

        deleteWithKey(url).andExpect(status().isNoContent());

        verify(imageStorageService).deleteAfterCommit("public-1");
        mockMvc.perform(get("/api/questions/" + question.id()))
                .andExpect(jsonPath("$.imageUrl").doesNotExist());
    }

    @Test
    void imageUploadWithoutKeyIsRejectedBeforeAnythingIsUploaded() throws Exception {
        Category category = saveCategory("Space");
        QuestionResponse question = saveQuestion(category);
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", new byte[] { 1, 2, 3 });

        mockMvc.perform(multipart("/api/questions/" + question.id() + "/image").file(file))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imageStorageService);
    }
}