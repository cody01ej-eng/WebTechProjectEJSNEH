package com.tcu.projectpulse.project.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ActiveWeekSetupRequest(
        @NotNull List<LocalDate> inactiveWeekStartDates
) {
}
