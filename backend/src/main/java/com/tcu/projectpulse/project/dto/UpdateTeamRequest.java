package com.tcu.projectpulse.project.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTeamRequest(
        @NotBlank String name,
        @NotBlank String description,
        String websiteUrl
) {
}
