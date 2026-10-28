package com.hscodeworks.quiz_backend.features.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OptionRequest(

        @NotBlank(message = "Option text is required")
        @Size(max = 500, message = "Option text must be at most 500 characters")
        String text,

        @NotNull(message = "Each option needs an explicit 'correct' flag")
        Boolean correct
) {
}