package com.tcu.projectpulse.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTeamRequest(
        @NotNull Long sectionId,
        @NotBlank String name,
        @NotBlank String description,
        String websiteUrl
) {
}
