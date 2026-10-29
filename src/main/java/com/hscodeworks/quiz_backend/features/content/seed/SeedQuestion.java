package com.hscodeworks.quiz_backend.features.content.seed;

import java.util.List;

import com.hscodeworks.quiz_backend.features.question.QuestionType;
import com.hscodeworks.quiz_backend.features.question.dto.OptionRequest;

public record SeedQuestion(
        String questionText,
        QuestionType type,
        String explanation,
        List<OptionRequest> options
) {
}