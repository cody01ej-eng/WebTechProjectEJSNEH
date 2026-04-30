package com.tcu.projectpulse.auth.dto;

import com.tcu.projectpulse.auth.domain.InvitationStatus;
import com.tcu.projectpulse.auth.domain.InvitationType;
import java.time.LocalDateTime;

public record InvitationResponse(
        Long id,
        String token,
        String email,
        InvitationType type,
        InvitationStatus status,
        String sectionName,
        LocalDateTime expiresAt,
        String message,
        String registrationUrl
) {
}
