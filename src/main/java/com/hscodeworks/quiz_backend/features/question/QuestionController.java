package com.hscodeworks.quiz_backend.features.question;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hscodeworks.quiz_backend.features.question.dto.QuestionRequest;
import com.hscodeworks.quiz_backend.features.question.dto.QuestionResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    
    @GetMapping("/categories/{categoryId}/questions")
    public List<QuestionResponse> getByCategory( @PathVariable Long categoryId ) {
        return questionService.getByCategory(categoryId);
    }


    @GetMapping("/questions/{id}")
    public QuestionResponse getById( @PathVariable Long id ) {
        return questionService.getById(id);
    }


    @PostMapping("/questions")
    public ResponseEntity<QuestionResponse> create( @Valid @RequestBody QuestionRequest request ) {
        QuestionResponse created = questionService.create(request);
        return ResponseEntity.created( URI.create("/api/questions/" + created.id()) ).body(created);
    }


    @PutMapping("/questions/{id}")
    public QuestionResponse update( @PathVariable Long id, @Valid @RequestBody QuestionRequest request ) {
        return questionService.update(id, request);
    }


    @DeleteMapping("/questions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete( @PathVariable Long id ) {
        questionService.delete(id);
    }


    // --------------------- Image APIs ---------------------
    @PostMapping(path = "/questions/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public QuestionResponse replaceImage( @PathVariable Long id, @RequestPart("file") MultipartFile file ) {
        return questionService.replaceImage(id, file);
    }

    
    @DeleteMapping("/questions/{id}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeImage(@PathVariable Long id) {
        questionService.removeImage(id);
    }
}