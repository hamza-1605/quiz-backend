package com.hscodeworks.quiz_backend.features.question.dto;

import com.hscodeworks.quiz_backend.features.question.QuestionOption;

public record OptionResponse(Long id, String text, boolean correct) {

    public static OptionResponse from(QuestionOption option) {
        return new OptionResponse(option.getId(), option.getOptionText(), option.isCorrect());
    }
}