package com.guildflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProgressApprovalRequest {
    @NotNull
    private Long entryId;
    private String mentorNotes;
}
