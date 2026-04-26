package com.tcu.projectpulse.requirement.dto;

import com.tcu.projectpulse.requirement.domain.ComponentType;
import com.tcu.projectpulse.requirement.domain.TraceabilityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTraceabilityLinkRequest(
        @NotNull Long requirementId,
        @NotNull ComponentType componentType,
        @NotBlank String componentName,
        @NotNull TraceabilityStatus status,
        @NotBlank String notes
) {
}
