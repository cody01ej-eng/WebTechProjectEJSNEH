package com.tcu.projectpulse.project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RubricCriterionRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") BigDecimal maxScore
) {
}
