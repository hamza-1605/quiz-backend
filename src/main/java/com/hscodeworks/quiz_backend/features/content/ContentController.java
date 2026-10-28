package com.hscodeworks.quiz_backend.features.content;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hscodeworks.quiz_backend.features.content.dto.SyncResponse;
import com.hscodeworks.quiz_backend.features.content.dto.VersionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentVersionService contentVersionService;
    private final ContentSyncService contentSyncService;

    
    @GetMapping("/version")
    public VersionResponse getVersion() {
        return new VersionResponse( contentVersionService.getCurrentVersion() );
    }

    
    @GetMapping("/sync")
    public SyncResponse sync() {
        return contentSyncService.getFullContent();
    }
}