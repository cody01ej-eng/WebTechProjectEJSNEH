package com.tcu.projectpulse.project.dto;

import com.tcu.projectpulse.project.domain.ActivityCategory;
import com.tcu.projectpulse.project.domain.ActivityProgressStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record WarActivityRequest(
        @NotNull(message = "Student ID is required") Long studentId,
        @NotNull(message = "Week start date is required") LocalDate weekStartDate,
        @NotNull(message = "Activity category is required") ActivityCategory category,
        @NotBlank(message = "Planned activity is required") String plannedActivity,
        @NotBlank(message = "Activity description is required") String description,
        @NotNull(message = "Planned hours are required")
        @DecimalMin(value = "0.00", message = "Planned hours must be zero or greater") BigDecimal plannedHours,
        @NotNull(message = "Actual hours are required")
        @DecimalMin(value = "0.00", message = "Actual hours must be zero or greater") BigDecimal actualHours,
        @NotNull(message = "Progress status is required") ActivityProgressStatus status
) {
}
