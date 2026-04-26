package com.tcu.projectpulse.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentRegistrationRequest(
        @NotBlank(message = "Invitation token is required") String token,
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be 100 characters or fewer") String firstName,
        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be 100 characters or fewer") String lastName,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must be 255 characters or fewer") String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters") String password
) {
}
