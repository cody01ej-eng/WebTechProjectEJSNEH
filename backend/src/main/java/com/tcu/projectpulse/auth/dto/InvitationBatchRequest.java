package com.tcu.projectpulse.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InvitationBatchRequest(
        Long sectionId,
        @NotEmpty(message = "At least one email is required")
        List<
                @NotBlank(message = "Invitation emails cannot be blank")
                @Email(message = "Invitation emails must be valid email addresses")
                String> emails,
        @NotBlank(message = "Invitation message is required") String message
) {
}
