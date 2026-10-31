package com.hscodeworks.quiz_backend.features.category;

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

import com.hscodeworks.quiz_backend.features.category.dto.CategoryRequest;
import com.hscodeworks.quiz_backend.features.category.dto.CategoryResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAll();
    }


    @GetMapping("/{id}")
    public CategoryResponse getById( @PathVariable Long id ) {
        return categoryService.getById(id);
    }


    @PostMapping
    public ResponseEntity<CategoryResponse> create( @Valid @RequestBody CategoryRequest request ) {
        CategoryResponse created = categoryService.create(request);
        return ResponseEntity.created( URI.create("/api/categories/" + created.id()) ).body(created);
                        // This URI gives the path as a response for the created entity 
    }


    @PutMapping("/{id}")
    public CategoryResponse update( @PathVariable Long id, @Valid @RequestBody CategoryRequest request ) {
        return categoryService.update(id, request);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }


    // --------------------- Image handlers ---------------------
    @PostMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CategoryResponse replaceImage( @PathVariable Long id, @RequestPart("file") MultipartFile file ) {
        return categoryService.replaceImage(id, file);
    }

    @DeleteMapping("/{id}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeImage( @PathVariable Long id ) {
        categoryService.removeImage(id);
    }
}