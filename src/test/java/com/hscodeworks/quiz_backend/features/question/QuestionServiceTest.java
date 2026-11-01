package com.hscodeworks.quiz_backend.features.question;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hscodeworks.quiz_backend.common.exception.BadRequestException;
import com.hscodeworks.quiz_backend.common.exception.ResourceNotFoundException;
import com.hscodeworks.quiz_backend.features.category.Category;
import com.hscodeworks.quiz_backend.features.category.CategoryRepository;
import com.hscodeworks.quiz_backend.features.content.ContentVersionService;
import com.hscodeworks.quiz_backend.features.image.ImageStorageService;
import com.hscodeworks.quiz_backend.features.question.dto.OptionRequest;
import com.hscodeworks.quiz_backend.features.question.dto.OptionResponse;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionRequest;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionResponse;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ContentVersionService contentVersionService;
    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private QuestionService questionService;



    // ---------- validation rules ----------

    @Test
    void createRejectsTrueFalseQuestionWithWrongOptionCount() {
        QuestionRequest request = request(QuestionType.TRUE_FALSE,
                option("True", true), option("False", false), option("Maybe", false), option("Never", false));

        assertThatThrownBy(() -> questionService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exactly 2 options");

        assertNothingWasWritten();
    }


    @Test
    void createRejectsMultipleChoiceQuestionWithWrongOptionCount() {
        QuestionRequest request = request(QuestionType.MULTIPLE_CHOICE,
                option("Venus", false), option("Mars", true), option("Jupiter", false));

        assertThatThrownBy(() -> questionService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exactly 4 options");

        assertNothingWasWritten();
    }


    @Test
    void createRejectsQuestionWithTwoCorrectOptions() {
        QuestionRequest request = request(QuestionType.TRUE_FALSE,
                option("True", true), option("False", true));

        assertThatThrownBy(() -> questionService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Exactly one option must be marked correct");

        assertNothingWasWritten();
    }


    @Test
    void createRejectsQuestionWithNoCorrectOption() {
        QuestionRequest request = request(QuestionType.TRUE_FALSE,
                option("True", false), option("False", false));

        assertThatThrownBy(() -> questionService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Exactly one option must be marked correct");

        assertNothingWasWritten();
    }


    @Test
    void createRejectsDuplicateOptionTextsIgnoringCaseAndSpaces() {
        QuestionRequest request = request(QuestionType.MULTIPLE_CHOICE,
                option("Mars", true), option("Venus", false), option("  MARS ", false), option("Jupiter", false));

        assertThatThrownBy(() -> questionService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("unique");

        assertNothingWasWritten();
    }


    @Test
    void createRejectsUnknownCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        QuestionRequest request = new QuestionRequest(99L, "Sample question?", QuestionType.TRUE_FALSE, null,
                List.of(option("True", true), option("False", false)));

        assertThatThrownBy(() -> questionService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category ID #99");

        assertNothingWasWritten();
    }



    // ---------- successful writes ----------

    @Test
    void createSavesQuestionAndBumpsVersionExactlyOnce() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category("Space", "#2E86DE")));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuestionResponse response = questionService.create(request(QuestionType.MULTIPLE_CHOICE,
                option("Venus", false), option("Mars", true), option("Jupiter", false), option("Mercury", false)));

        assertThat(response.options()).hasSize(4);
        assertThat(response.options())
                .filteredOn(OptionResponse::correct)
                .extracting(OptionResponse::text)
                .containsExactly("Mars");
        verify(contentVersionService, times(1)).incrementVersion();
    }


    @Test
    void createTrimsTextsAndTurnsBlankExplanationIntoNull() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category("Space", "#2E86DE")));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuestionRequest request = new QuestionRequest(1L, "  Is the Sun a star?  ", QuestionType.TRUE_FALSE, "   ",
                List.of(option(" True ", true), option("False", false)));

        QuestionResponse response = questionService.create(request);

        assertThat(response.questionText()).isEqualTo("Is the Sun a star?");
        assertThat(response.explanation()).isNull();
        assertThat(response.options()).extracting(OptionResponse::text).containsExactly("True", "False");
    }


    @Test
    void updateReplacesAllOptionsAndBumpsVersionOnce() {
        Category category = new Category("Space", "#2E86DE");
        Question existing = new Question();
        existing.setCategory(category);
        existing.setType(QuestionType.TRUE_FALSE);
        existing.setQuestionText("Old text");
        existing.replaceOptions(List.of(new QuestionOption("Old A", true), new QuestionOption("Old B", false)));

        when(questionRepository.findWithOptionsById(5L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        QuestionResponse response = questionService.update(5L, request(QuestionType.TRUE_FALSE,
                option("True", false), option("False", true)));

        assertThat(response.options()).extracting(OptionResponse::text).containsExactly("True", "False");
        assertThat(existing.getOptions()).extracting(QuestionOption::getOptionText).containsExactly("True", "False");
        verify(questionRepository).flush();
        verify(contentVersionService, times(1)).incrementVersion();
    }


    @Test
    void updateOfMissingQuestionThrowsNotFoundAndLeavesVersionAlone() {
        when(questionRepository.findWithOptionsById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.update(5L, request(QuestionType.TRUE_FALSE,
                option("True", true), option("False", false))))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(contentVersionService);
    }




    // ---------- helpers ----------

    private void assertNothingWasWritten() {
        verify(questionRepository, never()).save(any());
        verifyNoInteractions(contentVersionService);
    }

    private static QuestionRequest request(QuestionType type, OptionRequest... options) {
        return new QuestionRequest(1L, "Sample question?", type, null, List.of(options));
    }

    private static OptionRequest option(String text, boolean correct) {
        return new OptionRequest(text, correct);
    }
}