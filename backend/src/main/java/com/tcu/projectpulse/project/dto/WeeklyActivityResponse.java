package com.tcu.projectpulse.project.dto;

import com.tcu.projectpulse.project.domain.ActivityCategory;
import com.tcu.projectpulse.project.domain.ActivityProgressStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record WeeklyActivityResponse(
        Long id,
        Long studentId,
        String studentName,
        Long teamId,
        String teamName,
        LocalDate weekStartDate,
        ActivityCategory category,
        String plannedActivity,
        String description,
        BigDecimal plannedHours,
        BigDecimal actualHours,
        ActivityProgressStatus status
) {
}
