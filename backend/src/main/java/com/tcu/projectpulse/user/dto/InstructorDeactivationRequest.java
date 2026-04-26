package com.tcu.projectpulse.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InstructorDeactivationRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
