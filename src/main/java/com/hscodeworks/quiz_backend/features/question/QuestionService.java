package com.hscodeworks.quiz_backend.features.question;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hscodeworks.quiz_backend.common.exception.BadRequestException;
import com.hscodeworks.quiz_backend.common.exception.ResourceNotFoundException;
import com.hscodeworks.quiz_backend.features.category.Category;
import com.hscodeworks.quiz_backend.features.category.CategoryRepository;
import com.hscodeworks.quiz_backend.features.content.ContentVersionService;
import com.hscodeworks.quiz_backend.features.question.dto.OptionRequest;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionRequest;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final ContentVersionService contentVersionService;

    
    @Transactional(readOnly = true)
    public List<QuestionResponse> getByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category ID #" + categoryId + " not found");
        }
        return questionRepository.findByCategoryIdOrderById(categoryId)
                .stream()
                .map(QuestionResponse::from)
                .toList();
    }


    @Transactional(readOnly = true)
    public QuestionResponse getById(Long id) {
        return QuestionResponse.from(findOrThrow(id));
    }


    @Transactional
    public QuestionResponse create(QuestionRequest request) {
        validateOptions(request);
        Category category = findCategoryOrThrow(request.categoryId());

        Question question = new Question();
        applyFieldsIn(question, request, category);

        Question saved = questionRepository.save(question);
        contentVersionService.incrementVersion();
        return QuestionResponse.from(saved);
    }


    @Transactional
    public QuestionResponse update( Long id, QuestionRequest request ) {
        Question question = findOrThrow(id);
        validateOptions(request);
        Category category = findCategoryOrThrow(request.categoryId());

        applyFieldsIn(question, request, category);

        questionRepository.flush();
        contentVersionService.incrementVersion();
        return QuestionResponse.from(question);
    }


    @Transactional
    public void delete(Long id) {
        Question question = findOrThrow(id);
        questionRepository.delete(question);
        contentVersionService.incrementVersion();
    }


    // -------------------------- Helper Functions --------------------------

    private void applyFieldsIn(Question question, QuestionRequest request, Category category) {
        question.setCategory(category);
        question.setQuestionText(request.questionText().trim());
        question.setType(request.type());
        question.setExplanation(blankToNull(request.explanation()));
        question.replaceOptions(
                     request.options()
                            .stream()
                            .map( option -> new QuestionOption(option.text().trim(), option.correct()) )
                            .toList()
        );
    }


    private void validateOptions(QuestionRequest request) {
        List<OptionRequest> options = request.options();
        int expected = request.type().getRequiredOptions();

        if (options.size() != expected) {
            throw new BadRequestException(request.type() + " questions need exactly "
                    + expected + " options, but " + options.size() + " were provided");
        }

        long correctCount = options.stream().filter(OptionRequest::correct).count();
        if (correctCount != 1) {
            throw new BadRequestException( "Exactly one option must be marked correct (found " + correctCount + ")" );
        }

        Set<String> seen = new HashSet<>();
        for (OptionRequest option : options) {
            String normalized = option.text().trim().toLowerCase(Locale.ROOT);

            if (!seen.add(normalized)) {
                throw new BadRequestException( "Option texts must be unique within a question (duplicate: '" + option.text().trim() + "')");
            }
        }
    }


    private Question findOrThrow(Long id) {
        return questionRepository.findWithOptionsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question " + id + " not found"));
    }


    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category ID #" + categoryId + " not found"));
    }


    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}