package com.tcu.projectpulse.project.dto;

import java.time.LocalDate;
import java.util.List;

public record StudentWarReportResponse(
        Long studentId,
        String studentName,
        List<WeekActivities> weeks
) {
    public record WeekActivities(LocalDate weekStartDate, List<WeeklyActivityResponse> activities) {
    }
}
