package com.tcu.projectpulse.auth.dto;

public record CsrfTokenResponse(
        String headerName,
        String parameterName,
        String token
) {
}
