package com.tcu.projectpulse.project.dto;

import java.time.LocalDate;
import java.util.List;

public record SectionResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Long rubricId,
        String rubricName,
        List<WeekView> weeks,
        List<TeamView> teams
) {
    public record WeekView(Long id, LocalDate weekStartDate, boolean active) {
    }

    public record TeamView(Long id, String name) {
    }
}
