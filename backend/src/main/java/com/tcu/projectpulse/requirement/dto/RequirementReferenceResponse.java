package com.tcu.projectpulse.requirement.dto;

import com.tcu.projectpulse.requirement.domain.RequirementSource;

public record RequirementReferenceResponse(
        Long id,
        String referenceKey,
        String title,
        RequirementSource source,
        String summary
) {
}
