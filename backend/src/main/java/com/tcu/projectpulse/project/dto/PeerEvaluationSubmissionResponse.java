package com.tcu.projectpulse.project.dto;

import java.time.LocalDate;
import java.util.List;

public record PeerEvaluationSubmissionResponse(
        Long submissionId,
        Long authorId,
        String authorName,
        Long teamId,
        String teamName,
        LocalDate weekStartDate,
        List<EvaluationView> evaluations
) {
    public record EvaluationView(Long evaluateeId, String evaluateeName, String publicComment, String privateComment,
                                 List<ScoreView> scores) {
    }

    public record ScoreView(Long criterionId, String criterionName, Integer score) {
    }
}
