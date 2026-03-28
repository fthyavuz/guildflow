package com.guildflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveDayRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    private List<TaskEntry> entries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskEntry {
        @NotNull
        private Long taskId;
        private Double numericValue;
        private Boolean booleanValue;
    }
}
