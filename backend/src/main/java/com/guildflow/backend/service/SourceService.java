package com.guildflow.backend.service;

import com.guildflow.backend.dto.SourceRequest;
import com.guildflow.backend.dto.SourceResponse;
import com.guildflow.backend.model.Source;
import com.guildflow.backend.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;

    @Transactional(readOnly = true)
    public List<SourceResponse> getAllSources() {
        return sourceRepository.findAll().stream()
                .map(SourceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SourceResponse getSourceById(Long id) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Source not found"));
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
                .orElseThrow(() -> new RuntimeException("Source not found"));
        
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
