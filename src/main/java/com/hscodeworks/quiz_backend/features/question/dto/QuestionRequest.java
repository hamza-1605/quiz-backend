package com.hscodeworks.quiz_backend.features.question.dto;

import java.util.List;

import com.hscodeworks.quiz_backend.features.question.QuestionType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record QuestionRequest(

        @NotNull(message = "Category is required")
        Long categoryId,

        @NotBlank(message = "Question text is required")
        String questionText,

        @NotNull(message = "Type is required (TRUE_FALSE or MULTIPLE_CHOICE)")
        QuestionType type,

        String explanation,

        @NotEmpty(message = "Options are required")
        @Valid
        List<OptionRequest> options
) {
}