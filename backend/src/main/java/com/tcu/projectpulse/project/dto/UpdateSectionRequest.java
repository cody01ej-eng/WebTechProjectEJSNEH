package com.tcu.projectpulse.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateSectionRequest(
        @NotBlank String name,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull Long rubricId
) {
}
