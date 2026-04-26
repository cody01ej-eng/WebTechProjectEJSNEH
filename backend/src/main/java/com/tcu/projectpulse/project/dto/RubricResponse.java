package com.tcu.projectpulse.project.dto;

import java.math.BigDecimal;
import java.util.List;

public record RubricResponse(
        Long id,
        String name,
        List<Criterion> criteria
) {
    public record Criterion(Long id, String name, String description, BigDecimal maxScore, Integer displayOrder) {
    }
}
