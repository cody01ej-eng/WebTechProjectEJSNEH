package com.tcu.projectpulse.project.dto;

import jakarta.validation.constraints.NotNull;

public record PeerEvaluationScoreRequest(
        @NotNull Long criterionId,
        @NotNull Integer score
) {
}
