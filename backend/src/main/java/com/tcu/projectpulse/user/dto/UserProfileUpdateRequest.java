package com.tcu.projectpulse.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank @Size(max = 100) String firstName,
        @Size(max = 10) String middleInitial,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email
) {
}
