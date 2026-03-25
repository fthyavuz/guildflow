package com.guildflow.backend.service;

import com.guildflow.backend.dto.PendingProgressResponse;
import com.guildflow.backend.dto.ProgressApprovalRequest;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.model.TaskProgress;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.ProgressEntryStatus;
import com.guildflow.backend.repository.TaskProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressApprovalService {

    private final TaskProgressRepository taskProgressRepository;

    public List<PendingProgressResponse> getPendingEntries() {
        return taskProgressRepository.findByStatusWithDetails(ProgressEntryStatus.PENDING)
                .stream()
                .map(PendingProgressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approve(ProgressApprovalRequest request, User reviewer) {
        TaskProgress entry = taskProgressRepository.findById(request.getEntryId())
                .orElseThrow(() -> new EntityNotFoundException("Progress entry not found"));
        entry.setStatus(ProgressEntryStatus.APPROVED);
        entry.setMentorNotes(request.getMentorNotes());
        entry.setReviewedBy(reviewer);
        entry.setReviewedAt(LocalDateTime.now());
        taskProgressRepository.save(entry);
    }

    @Transactional
    public void reject(ProgressApprovalRequest request, User reviewer) {
        TaskProgress entry = taskProgressRepository.findById(request.getEntryId())
                .orElseThrow(() -> new EntityNotFoundException("Progress entry not found"));
        entry.setStatus(ProgressEntryStatus.REJECTED);
        entry.setMentorNotes(request.getMentorNotes());
        entry.setReviewedBy(reviewer);
        entry.setReviewedAt(LocalDateTime.now());
        taskProgressRepository.save(entry);
    }
}
