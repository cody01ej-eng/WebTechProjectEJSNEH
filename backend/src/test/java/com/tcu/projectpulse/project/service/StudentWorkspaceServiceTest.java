package com.tcu.projectpulse.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.auth.service.AuthenticatedUserService;
import com.tcu.projectpulse.project.domain.ActiveWeek;
import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.domain.RubricCriterion;
import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.domain.SeniorDesignTeam;
import com.tcu.projectpulse.project.dto.StudentWorkspaceContextResponse;
import com.tcu.projectpulse.project.repository.ActiveWeekRepository;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StudentWorkspaceServiceTest {

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private ActiveWeekRepository activeWeekRepository;

    private StudentWorkspaceService studentWorkspaceService;

    @BeforeEach
    void setUp() {
        studentWorkspaceService = new StudentWorkspaceService(
                authenticatedUserService,
                userAccountRepository,
                activeWeekRepository,
                Clock.fixed(Instant.parse("2026-04-22T12:00:00Z"), ZoneId.of("America/Chicago"))
        );
    }

    @Test
    void getCurrentStudentWorkspaceContextBuildsGuidedSubmissionContext() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        ReflectionTestUtils.setField(rubric, "id", 5L);

        RubricCriterion quality = new RubricCriterion(
                rubric,
                "Quality of work",
                "Rate the quality of this teammate's work.",
                BigDecimal.TEN,
                1
        );
        RubricCriterion initiative = new RubricCriterion(
                rubric,
                "Initiative",
                "Rate the teammate's initiative.",
                BigDecimal.TEN,
                2
        );
        ReflectionTestUtils.setField(quality, "id", 11L);
        ReflectionTestUtils.setField(initiative, "id", 12L);
        rubric.addCriterion(quality);
        rubric.addCriterion(initiative);

        SeniorDesignSection section = new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                rubric
        );
        ReflectionTestUtils.setField(section, "id", 7L);

        SeniorDesignTeam team = new SeniorDesignTeam(section, "Pulse Team", "Core product team", "https://pulse.example");
        ReflectionTestUtils.setField(team, "id", 21L);

        UserAccount currentStudent = new UserAccount(
                "Jane",
                null,
                "Doe",
                "jane@tcu.edu",
                "{noop}",
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                section
        );
        ReflectionTestUtils.setField(currentStudent, "id", 31L);
        currentStudent.assignToTeam(team);

        UserAccount teammate = new UserAccount(
                "Lily",
                null,
                "Fisher",
                "lily@tcu.edu",
                "{noop}",
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                section
        );
        ReflectionTestUtils.setField(teammate, "id", 32L);
        teammate.assignToTeam(team);

        when(authenticatedUserService.requireCurrentUserAccount()).thenReturn(currentStudent);
        when(activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(7L)).thenReturn(List.of(
                new ActiveWeek(section, LocalDate.of(2026, 4, 13), true),
                new ActiveWeek(section, LocalDate.of(2026, 4, 20), true),
                new ActiveWeek(section, LocalDate.of(2026, 4, 27), true)
        ));
        when(userAccountRepository.findByAssignedTeamId(21L)).thenReturn(List.of(currentStudent, teammate));

        StudentWorkspaceContextResponse response = studentWorkspaceService.getCurrentStudentWorkspaceContext();

        assertThat(response.studentId()).isEqualTo(31L);
        assertThat(response.sectionName()).isEqualTo("2026-2027");
        assertThat(response.teamName()).isEqualTo("Pulse Team");
        assertThat(response.suggestedWarWeekStartDate()).isEqualTo(LocalDate.of(2026, 4, 20));
        assertThat(response.suggestedPeerEvaluationWeekStartDate()).isEqualTo(LocalDate.of(2026, 4, 13));
        assertThat(response.activeWeeks()).containsExactly(
                LocalDate.of(2026, 4, 13),
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 4, 27)
        );
        assertThat(response.teammates()).extracting(StudentWorkspaceContextResponse.TeammateView::name)
                .containsExactly("Jane Doe", "Lily Fisher");
        assertThat(response.teammates()).extracting(StudentWorkspaceContextResponse.TeammateView::self)
                .containsExactly(true, false);
        assertThat(response.rubricCriteria()).extracting(StudentWorkspaceContextResponse.RubricCriterionView::name)
                .containsExactly("Quality of work", "Initiative");
    }
}
