package com.hscodeworks.quiz_backend.features.content;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ContentVersionService {

    private static final Short SINGLE_ROW_ID = 1;
    private final ContentVersionRepository repository;
    
    @Transactional(readOnly = true)
    public long getCurrentVersion() {
        return repository.findById(SINGLE_ROW_ID)
                        .orElseThrow(() -> new IllegalStateException("content_version row is missing"))
                        .getVersion();
    }

    
    @Transactional
    public void incrementVersion() {
        repository.increment(SINGLE_ROW_ID);
    }
}