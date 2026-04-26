package com.tcu.projectpulse.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record SubmitPeerEvaluationRequest(
        @NotNull Long authorId,
        @NotNull LocalDate weekStartDate,
        @NotEmpty List<@Valid PeerEvaluationItemRequest> evaluations
) {
}
