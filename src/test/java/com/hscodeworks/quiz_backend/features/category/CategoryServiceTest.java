package com.hscodeworks.quiz_backend.features.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hscodeworks.quiz_backend.common.exception.ConflictException;
import com.hscodeworks.quiz_backend.common.exception.ResourceNotFoundException;
import com.hscodeworks.quiz_backend.features.category.dto.CategoryRequest;
import com.hscodeworks.quiz_backend.features.category.dto.CategoryResponse;
import com.hscodeworks.quiz_backend.features.content.ContentVersionService;
import com.hscodeworks.quiz_backend.features.image.ImageStorageService;
import com.hscodeworks.quiz_backend.features.question.QuestionRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private ContentVersionService contentVersionService;
    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createRejectsDuplicateNameAndLeavesVersionAlone() {
        when(categoryRepository.existsByNameIgnoreCase("Space")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(new CategoryRequest("Space", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any());
        verifyNoInteractions(contentVersionService);
    }

    @Test
    void createTrimsNameSavesAndBumpsVersionOnce() {
        when(categoryRepository.existsByNameIgnoreCase("Space")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.create(new CategoryRequest("  Space  ", "#2E86DE"));

        assertThat(response.name()).isEqualTo("Space");
        verify(contentVersionService, times(1)).incrementVersion();
    }

    @Test
    void updateRejectsNameUsedByAnotherCategory() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(new Category("Islamic", null)));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Space", 2L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(2L, new CategoryRequest("Space", null)))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(contentVersionService);
    }

    @Test
    void deleteIsBlockedWhileCategoryStillHasQuestions() {
        Category category = new Category("Space", null);
        category.setImagePublicId("img-123");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(questionRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("still has questions");

        verify(categoryRepository, never()).delete(any());
        verifyNoInteractions(contentVersionService);
        verifyNoInteractions(imageStorageService);
    }

    @Test
    void deleteRemovesCategoryBumpsVersionAndSchedulesImageCleanup() {
        Category category = new Category("Space", null);
        category.setImagePublicId("img-123");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(questionRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
        verify(imageStorageService).deleteAfterCommit("img-123");
        verify(contentVersionService, times(1)).incrementVersion();
    }

    @Test
    void deleteOfMissingCategoryThrowsNotFound() {
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(9L))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(contentVersionService);
    }
}