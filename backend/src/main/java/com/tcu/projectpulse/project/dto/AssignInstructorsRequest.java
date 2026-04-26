package com.tcu.projectpulse.project.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AssignInstructorsRequest(
        @NotEmpty List<Long> instructorIds
) {
}
