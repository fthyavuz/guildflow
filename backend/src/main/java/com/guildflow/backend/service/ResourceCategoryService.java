package com.guildflow.backend.service;

import com.guildflow.backend.dto.ResourceCategoryRequest;
import com.guildflow.backend.dto.ResourceCategoryResponse;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ValidationException;
import com.guildflow.backend.model.ResourceCategory;
import com.guildflow.backend.repository.ResourceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceCategoryService {

    private final ResourceCategoryRepository categoryRepository;

    public List<ResourceCategoryResponse> getAllActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(ResourceCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ResourceCategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(ResourceCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResourceCategoryResponse create(ResourceCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ValidationException("A category with this name already exists");
        }
        ResourceCategory category = ResourceCategory.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();
        return ResourceCategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public ResourceCategoryResponse update(Long id, ResourceCategoryRequest request) {
        ResourceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        return ResourceCategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public void deactivate(Long id) {
        ResourceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
    }
}
