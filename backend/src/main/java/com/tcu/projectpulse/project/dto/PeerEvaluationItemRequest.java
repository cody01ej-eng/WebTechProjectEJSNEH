package com.tcu.projectpulse.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PeerEvaluationItemRequest(
        @NotNull Long evaluateeId,
        String publicComment,
        String privateComment,
        @NotEmpty List<@Valid PeerEvaluationScoreRequest> scores
) {
}
