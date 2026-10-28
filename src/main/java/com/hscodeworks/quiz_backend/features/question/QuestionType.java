package com.hscodeworks.quiz_backend.features.question;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionType {
    TRUE_FALSE(2),
    MULTIPLE_CHOICE(4);

    private final int requiredOptions;
}