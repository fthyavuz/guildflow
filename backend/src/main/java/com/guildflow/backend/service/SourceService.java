package com.guildflow.backend.service;

import com.guildflow.backend.dto.SourceRequest;
import com.guildflow.backend.dto.SourceResponse;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.model.Source;
import com.guildflow.backend.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;

    @Transactional(readOnly = true)
    public Page<SourceResponse> getAllSources(Pageable pageable) {
        return sourceRepository.findAll(pageable).map(SourceResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public SourceResponse getSourceById(Long id) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Source not found"));
        return SourceResponse.fromEntity(source);
    }

    @Transactional
    public SourceResponse createSource(SourceRequest request) {
        Source source = Source.builder()
                .title(request.getTitle())
                .type(request.getType())
                .language(request.getLanguage())
                .part(request.getPart())
                .totalPages(request.getTotalPages())
                .totalMinutes(request.getTotalMinutes())
                .build();

        return SourceResponse.fromEntity(sourceRepository.save(source));
    }

    @Transactional
    public SourceResponse updateSource(Long id, SourceRequest request) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Source not found"));

        source.setTitle(request.getTitle());
        source.setType(request.getType());
        source.setLanguage(request.getLanguage());
        source.setPart(request.getPart());
        source.setTotalPages(request.getTotalPages());
        source.setTotalMinutes(request.getTotalMinutes());

        return SourceResponse.fromEntity(sourceRepository.save(source));
    }

    @Transactional
    public void deleteSource(Long id) {
        sourceRepository.deleteById(id);
    }
}
