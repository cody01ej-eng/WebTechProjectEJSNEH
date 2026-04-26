package com.tcu.projectpulse.project.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StudentWorkspaceContextResponse(
        Long studentId,
        String studentName,
        String email,
        Long sectionId,
        String sectionName,
        Long teamId,
        String teamName,
        Long rubricId,
        String rubricName,
        LocalDate suggestedWarWeekStartDate,
        LocalDate suggestedPeerEvaluationWeekStartDate,
        List<LocalDate> activeWeeks,
        List<TeammateView> teammates,
        List<RubricCriterionView> rubricCriteria
) {
    public record TeammateView(Long id, String name, boolean self) {
    }

    public record RubricCriterionView(Long id, String name, String description, BigDecimal maxScore, Integer displayOrder) {
    }
}
