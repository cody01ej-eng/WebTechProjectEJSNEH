package com.tcu.projectpulse.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.domain.SeniorDesignTeam;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService(authenticatedUserService);
    }

    @Test
    void teamWarReportAllowsStudentsOnlyForTheirAssignedTeam() {
        SeniorDesignSection section = new SeniorDesignSection("2026-2027", LocalDate.of(2026, 8, 24), LocalDate.of(2027, 5, 1), null);
        SeniorDesignTeam assignedTeam = new SeniorDesignTeam(section, "Pulse", "Core team", null);
        ReflectionTestUtils.setField(assignedTeam, "id", 14L);
        UserAccount student = new UserAccount(
                "Jane",
                null,
                "Doe",
                "jane@tcu.edu",
                "{noop}",
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                section
        );
        student.assignToTeam(assignedTeam);

        when(authenticatedUserService.requireCurrentUserRole()).thenReturn(UserRole.STUDENT);
        when(authenticatedUserService.requireCurrentUserAccount()).thenReturn(student);

        assertThat(authorizationService.canAccessTeamWarReport(14L)).isTrue();
        assertThat(authorizationService.canAccessTeamWarReport(99L)).isFalse();
    }
}
