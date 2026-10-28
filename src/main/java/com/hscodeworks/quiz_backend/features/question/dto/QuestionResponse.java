package com.hscodeworks.quiz_backend.features.question.dto;

import java.util.List;

import com.hscodeworks.quiz_backend.features.question.Question;
import com.hscodeworks.quiz_backend.features.question.QuestionType;

public record QuestionResponse(
        Long id,
        Long categoryId,
        String questionText,
        QuestionType type,
        String explanation,
        String imageUrl,
        List<OptionResponse> options
) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
            question.getId(),
            question.getCategory().getId(),
            question.getQuestionText(),
            question.getType(),
            question.getExplanation(),
            question.getImageUrl(),
            question.getOptions()
                    .stream()
                    .map(OptionResponse::from)
                    .toList()
        );
    }
}