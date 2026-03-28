package com.guildflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyProgressEntry {
    private LocalDate date;
    private Double value;
}
