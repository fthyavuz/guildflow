package com.guildflow.backend.controller;

import com.guildflow.backend.dto.SourceRequest;
import com.guildflow.backend.dto.SourceResponse;
import com.guildflow.backend.service.SourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    @GetMapping
    public ResponseEntity<Page<SourceResponse>> getAllSources(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(sourceService.getAllSources(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceResponse> getSourceById(@PathVariable Long id) {
        return ResponseEntity.ok(sourceService.getSourceById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<SourceResponse> createSource(@Valid @RequestBody SourceRequest request) {
        return ResponseEntity.ok(sourceService.createSource(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<SourceResponse> updateSource(@PathVariable Long id, @Valid @RequestBody SourceRequest request) {
        return ResponseEntity.ok(sourceService.updateSource(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> deleteSource(@PathVariable Long id) {
        sourceService.deleteSource(id);
        return ResponseEntity.noContent().build();
    }
}
