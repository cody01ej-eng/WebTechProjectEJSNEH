package com.tcu.projectpulse.project.dto;

import java.math.BigDecimal;

public record CriterionAverage(
        Long criterionId,
        String criterionName,
        BigDecimal averageScore
) {
}
