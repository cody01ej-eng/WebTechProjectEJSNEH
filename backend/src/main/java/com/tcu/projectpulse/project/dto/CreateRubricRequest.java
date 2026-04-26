package com.tcu.projectpulse.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateRubricRequest(
        @NotBlank String name,
        @NotEmpty List<@Valid RubricCriterionRequest> criteria
) {
}
