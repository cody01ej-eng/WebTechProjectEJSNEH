package com.tcu.projectpulse.project.dto;

import java.time.LocalDate;
import java.util.List;

public record TeamWarReportResponse(
        Long teamId,
        String teamName,
        LocalDate weekStartDate,
        List<String> missingStudents,
        List<StudentActivityGroup> students
) {
    public record StudentActivityGroup(Long studentId, String studentName, List<WeeklyActivityResponse> activities) {
    }
}
