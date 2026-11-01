package com.hscodeworks.quiz_backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import com.hscodeworks.quiz_backend.TestcontainersConfiguration;
import com.hscodeworks.quiz_backend.features.category.Category;
import com.hscodeworks.quiz_backend.features.category.CategoryRepository;
import com.hscodeworks.quiz_backend.features.content.ContentVersionService;
import com.hscodeworks.quiz_backend.features.image.ImageStorageService;
import com.hscodeworks.quiz_backend.features.question.QuestionRepository;
import com.hscodeworks.quiz_backend.features.question.QuestionService;
import com.hscodeworks.quiz_backend.features.question.QuestionType;
import com.hscodeworks.quiz_backend.features.question.dto.OptionRequest;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionRequest;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest(properties = {
        "app.seed.enabled=false",
        "app.security.api-key=" + AbstractIntegrationTest.API_KEY
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
abstract class AbstractIntegrationTest {

    protected static final String API_KEY = "integration-test-key";
    protected static final String KEY_HEADER = "X-API-Key";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected QuestionRepository questionRepository;
    @Autowired
    protected QuestionService questionService;
    @Autowired
    protected ContentVersionService contentVersionService;

    @PersistenceContext
    protected EntityManager entityManager;

    // Cloudinary is never called in tests
    @MockitoBean
    protected ImageStorageService imageStorageService;

    @BeforeEach
    void cleanDatabase() {
        questionRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    // ---------- arrange helpers ----------

    protected long currentVersion() {
        return contentVersionService.getCurrentVersion();
    }

    protected Category saveCategory(String name) {
        return categoryRepository.save(new Category(name, "#2E86DE"));
    }

    /** A valid true/false question with 2 options. */
    protected QuestionResponse saveQuestion(Category category) {
        return questionService.create(new QuestionRequest(
                category.getId(), "Sample question?", QuestionType.TRUE_FALSE, null,
                List.of(new OptionRequest("True", true), new OptionRequest("False", false))));
    }

    protected long optionRowCount() {
        return ((Number) entityManager
                .createNativeQuery("select count(*) from question_option")
                .getSingleResult()).longValue();
    }

    protected static String mcqJson(long categoryId, String text) {
        return """
                {"categoryId": %d, "questionText": "%s", "type": "MULTIPLE_CHOICE", "explanation": "Because.",
                 "options": [
                   {"text": "A", "correct": true}, {"text": "B", "correct": false},
                   {"text": "C", "correct": false}, {"text": "D", "correct": false}]}
                """.formatted(categoryId, text);
    }

    // ---------- request helpers (all send the API key) ----------

    protected ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).header(KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions putJson(String url, String json) throws Exception {
        return mockMvc.perform(put(url).header(KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions deleteWithKey(String url) throws Exception {
        return mockMvc.perform(delete(url).header(KEY_HEADER, API_KEY));
    }

    protected ResultActions uploadImage(String url) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", new byte[] { 1, 2, 3 });
        return mockMvc.perform(multipart(url).file(file).header(KEY_HEADER, API_KEY));
    }

    protected static ResultMatcher bodyContains(String text) {
        return result -> assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).contains(text);
    }
}