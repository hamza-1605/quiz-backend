package com.hscodeworks.quiz_backend.features.content.seed;

import java.util.List;

import com.hscodeworks.quiz_backend.features.category.dto.CategoryRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SeedFile(

        @NotNull(message = "category is required")
        @Valid
        CategoryRequest category,

        @NotEmpty(message = "questions must not be empty")
        List<SeedQuestion> questions
) {
}