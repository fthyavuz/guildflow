package com.guildflow.backend.controller;

import com.guildflow.backend.dto.ResourceCategoryRequest;
import com.guildflow.backend.dto.ResourceCategoryResponse;
import com.guildflow.backend.service.ResourceCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource-categories")
@RequiredArgsConstructor
public class ResourceCategoryController {

    private final ResourceCategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<ResourceCategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getAllActive());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResourceCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceCategoryResponse> createCategory(
            @Valid @RequestBody ResourceCategoryRequest request) {
        return ResponseEntity.status(201).body(categoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ResourceCategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {
        categoryService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
