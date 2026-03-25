package com.guildflow.backend.service;

import com.guildflow.backend.dto.SourceRequest;
import com.guildflow.backend.dto.SourceResponse;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.model.ResourceCategory;
import com.guildflow.backend.model.Source;
import com.guildflow.backend.repository.ResourceCategoryRepository;
import com.guildflow.backend.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;
    private final ResourceCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<SourceResponse> getAllSources(Pageable pageable) {
        return sourceRepository.findAllWithCategory(pageable).map(SourceResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public SourceResponse getSourceById(Long id) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Source not found"));
        return SourceResponse.fromEntity(source);
    }

    @Transactional
    public SourceResponse createSource(SourceRequest request) {
        ResourceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Source source = Source.builder()
                .title(request.getTitle())
                .category(category)
                .trackingType(request.getTrackingType())
                .totalCapacity(request.getTotalCapacity())
                .dailyLimit(request.getDailyLimit())
                .language(request.getLanguage())
                .part(request.getPart())
                .build();

        return SourceResponse.fromEntity(sourceRepository.save(source));
    }

    @Transactional
    public SourceResponse updateSource(Long id, SourceRequest request) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Source not found"));

        ResourceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        source.setTitle(request.getTitle());
        source.setCategory(category);
        source.setTrackingType(request.getTrackingType());
        source.setTotalCapacity(request.getTotalCapacity());
        source.setDailyLimit(request.getDailyLimit());
        source.setLanguage(request.getLanguage());
        source.setPart(request.getPart());

        return SourceResponse.fromEntity(sourceRepository.save(source));
    }

    @Transactional
    public void deleteSource(Long id) {
        sourceRepository.deleteById(id);
    }
}
