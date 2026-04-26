package com.tcu.projectpulse.project.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StudentPeerEvaluationReportResponse(
        Long studentId,
        String studentName,
        List<WeekReport> weeks
) {
    public record WeekReport(
            LocalDate weekStartDate,
            BigDecimal averageGrade,
            List<CriterionAverage> criterionAverages,
            List<String> publicComments
    ) {
    }
}
