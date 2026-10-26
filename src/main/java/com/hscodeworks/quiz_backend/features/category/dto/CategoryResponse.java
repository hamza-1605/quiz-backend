package com.hscodeworks.quiz_backend.features.category.dto;

import com.hscodeworks.quiz_backend.features.category.Category;

public record CategoryResponse(Long id, String name, String imageUrl, String color) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getImageUrl(),
            category.getColor()
        );
    }
}