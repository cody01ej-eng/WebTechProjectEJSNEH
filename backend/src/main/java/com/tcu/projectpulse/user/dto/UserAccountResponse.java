package com.tcu.projectpulse.user.dto;

import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import java.util.List;

public record UserAccountResponse(
        Long id,
        String firstName,
        String middleInitial,
        String lastName,
        String email,
        UserRole role,
        UserStatus status,
        Long sectionId,
        String sectionName,
        Long assignedTeamId,
        String assignedTeamName,
        List<String> supervisedTeams
) {
}
