package com.tcu.projectpulse.project.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SectionPeerEvaluationReportResponse(
        Long sectionId,
        String sectionName,
        LocalDate weekStartDate,
        List<String> missingEvaluators,
        List<StudentPeerSummary> students
) {
    public record StudentPeerSummary(
            Long studentId,
            String studentName,
            BigDecimal averageGrade,
            List<CommentView> comments,
            List<DetailedEvaluation> details
    ) {
    }

    public record CommentView(String evaluatorName, String publicComment, String privateComment) {
    }

    public record DetailedEvaluation(String evaluatorName, List<CriterionAverage> scores, String publicComment,
                                     String privateComment) {
    }
}
