package com.tcu.projectpulse.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InstructorRegistrationRequest(
        @NotBlank(message = "Invitation token is required") String token,
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be 100 characters or fewer") String firstName,
        @Size(max = 10, message = "Middle initial must be 10 characters or fewer") String middleInitial,
        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be 100 characters or fewer") String lastName,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters") String password,
        @NotBlank(message = "Password confirmation is required")
        @Size(min = 8, max = 255, message = "Password confirmation must be between 8 and 255 characters") String confirmPassword
) {
}
