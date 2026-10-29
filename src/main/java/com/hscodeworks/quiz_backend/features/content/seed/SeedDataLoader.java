package com.hscodeworks.quiz_backend.features.content.seed;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hscodeworks.quiz_backend.features.category.CategoryRepository;
import com.hscodeworks.quiz_backend.features.category.CategoryService;
import com.hscodeworks.quiz_backend.features.category.dto.CategoryResponse;
import com.hscodeworks.quiz_backend.features.question.QuestionService;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedDataLoader implements ApplicationRunner {

    private static final String SEED_PATTERN = "classpath:seed/*.json";

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final QuestionService questionService;
    private final ResourcePatternResolver resourceResolver;
    private final JsonMapper jsonMapper;
    private final Validator validator;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws IOException {
        Resource[] files = resourceResolver.getResources(SEED_PATTERN);
        
        if (files.length == 0) {
            log.warn("No seed files found at {}", SEED_PATTERN);
            return;
        }
        Arrays.sort(files, Comparator.comparing(Resource::getFilename));

        int categoriesLoaded = 0;
        int questionsLoaded = 0;
        for (Resource file : files) {
            OptionalInt loaded = loadFile( file );
            if (loaded.isPresent()) {
                categoriesLoaded++;
                questionsLoaded += loaded.getAsInt();
            }
        }
        log.info("Seed finished: {} new categories, {} new questions", categoriesLoaded, questionsLoaded);
    }

    
    
    // ------------------------- Helper functions for RUN ------------------------- 
    
    private OptionalInt loadFile(Resource resource) throws IOException {
        String fileName = resource.getFilename();

        SeedFile file;
        try (InputStream in = resource.getInputStream()) {
            file = jsonMapper.readValue(in, SeedFile.class);
        } 
        catch (JacksonException ex) {
            throw new IllegalStateException("Could not parse seed file " + fileName + ": " + ex.getMessage(), ex);
        }
        validate(file, fileName);

        String categoryName = file.category().name().trim();
        if (categoryRepository.existsByNameIgnoreCase(categoryName)) {
            log.info("Skipped {}: category '{}' already exists", fileName, categoryName);
            return OptionalInt.empty();
        }

        CategoryResponse category = categoryService.create(file.category());

        int number = 0;
        for (SeedQuestion question : file.questions()) {
            number++;
            String where = fileName + ", question #" + number;

            QuestionRequest request = new QuestionRequest(
                    category.id(),
                    question.questionText(),
                    question.type(),
                    question.explanation(),
                    question.options()
            );
            validate(request, where);

            try {
                questionService.create(request);
            } 
            catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid seed data in " + where + ": " + ex.getMessage(), ex);
            }
        }

        log.info("Loaded category '{}' with {} questions from {}", category.name(), number, fileName);
        return OptionalInt.of(number);
    }

    
    private <T> void validate(T target, String where) {
        Set<ConstraintViolation<T>> violations = validator.validate( target );

        if (!violations.isEmpty()) {
            String details =  violations.stream()
                                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                                        .sorted()
                                        .collect(Collectors.joining("; "));

            throw new IllegalStateException("Invalid seed data in " + where + ": " + details);
        }
    }
}