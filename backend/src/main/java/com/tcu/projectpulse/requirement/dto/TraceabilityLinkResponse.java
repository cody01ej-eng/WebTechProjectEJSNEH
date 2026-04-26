package com.tcu.projectpulse.requirement.dto;

import com.tcu.projectpulse.requirement.domain.ComponentType;
import com.tcu.projectpulse.requirement.domain.TraceabilityStatus;

public record TraceabilityLinkResponse(
        Long id,
        Long requirementId,
        String requirementKey,
        String requirementTitle,
        ComponentType componentType,
        String componentName,
        TraceabilityStatus status,
        String notes
) {
}
