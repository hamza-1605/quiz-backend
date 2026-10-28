package com.hscodeworks.quiz_backend.features.category;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hscodeworks.quiz_backend.features.category.dto.CategoryRequest;
import com.hscodeworks.quiz_backend.features.category.dto.CategoryResponse;
import com.hscodeworks.quiz_backend.features.content.ContentVersionService;
import com.hscodeworks.quiz_backend.features.question.QuestionRepository;
import com.hscodeworks.quiz_backend.common.exception.ConflictException;
import com.hscodeworks.quiz_backend.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ContentVersionService contentVersionService;
    private final QuestionRepository questionRepository;


    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll( Sort.by("name") )
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }


    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return CategoryResponse.from( findOrThrow(id) );
    }


    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("A category named '" + name + "' already exists");
        }

        Category saved = categoryRepository.save(new Category(name, request.color()));
        contentVersionService.incrementVersion();
        return CategoryResponse.from(saved);
    }


    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOrThrow(id);

        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("A category named '" + name + "' already exists");
        }

        category.setName(name);
        category.setColor(request.color());
        contentVersionService.incrementVersion();
        return CategoryResponse.from(category);
    }

    
    @Transactional
    public void delete(Long id) {
        Category category = findOrThrow(id);
        if ( questionRepository.existsByCategoryId(id) ) {
            throw new ConflictException( "Category '" + category.getName() + "' still has questions. Delete them first." );
        }
        categoryRepository.delete(category);
        contentVersionService.incrementVersion();
    }

    
    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category of ID #" + id + " not found"));
    }

}