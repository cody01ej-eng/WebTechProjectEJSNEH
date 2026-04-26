package com.tcu.projectpulse.requirement.dto;

import com.tcu.projectpulse.requirement.domain.RequirementSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRequirementReferenceRequest(
        @NotBlank String referenceKey,
        @NotBlank String title,
        @NotNull RequirementSource source,
        @NotBlank String summary
) {
}
