package com.hscodeworks.quiz_backend.features.content;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.hscodeworks.quiz_backend.features.category.CategoryRepository;
import com.hscodeworks.quiz_backend.features.category.dto.CategoryResponse;
import com.hscodeworks.quiz_backend.features.content.dto.SyncResponse;
import com.hscodeworks.quiz_backend.features.question.QuestionRepository;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentSyncService {

    private final ContentVersionService contentVersionService;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public SyncResponse getFullContent() {
        long version = contentVersionService.getCurrentVersion();

        List<CategoryResponse> categories  =  categoryRepository.findAll(Sort.by("id"))
                                                                .stream()
                                                                .map(CategoryResponse::from)
                                                                .toList();

        List<QuestionResponse> questions   =  questionRepository.findAllByOrderById()
                                                                .stream()
                                                                .map(QuestionResponse::from)
                                                                .toList();

        return new SyncResponse(version, categories, questions);
    }
}