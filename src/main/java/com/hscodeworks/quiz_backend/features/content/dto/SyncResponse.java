package com.hscodeworks.quiz_backend.features.content.dto;

import java.util.List;

import com.hscodeworks.quiz_backend.features.category.dto.CategoryResponse;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionResponse;

public record SyncResponse(
    long version,
    List<CategoryResponse> categories,
    List<QuestionResponse> questions
) {
}