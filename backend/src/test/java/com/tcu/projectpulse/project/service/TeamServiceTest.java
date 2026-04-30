package com.tcu.projectpulse.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.project.domain.PeerEvaluationSubmission;
import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.domain.SeniorDesignTeam;
import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.domain.TeamInstructorAssignment;
import com.tcu.projectpulse.project.domain.WeeklyActivity;
import com.tcu.projectpulse.project.dto.TeamDeletionResponse;
import com.tcu.projectpulse.project.dto.TeamResponse;
import com.tcu.projectpulse.project.repository.PeerEvaluationSubmissionRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignTeamRepository;
import com.tcu.projectpulse.project.repository.TeamInstructorAssignmentRepository;
import com.tcu.projectpulse.project.repository.WeeklyActivityRepository;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private SeniorDesignTeamRepository teamRepository;

    @Mock
    private TeamInstructorAssignmentRepository assignmentRepository;

    @Mock
    private WeeklyActivityRepository weeklyActivityRepository;

    @Mock
    private PeerEvaluationSubmissionRepository peerEvaluationSubmissionRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private TeamService teamService;

    @Test
    void removeStudentClearsTheCurrentTeamAssignment() {
        SeniorDesignSection section = new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                new Rubric("Rubric")
        );
        ReflectionTestUtils.setField(section, "id", 1L);
        SeniorDesignTeam team = new SeniorDesignTeam(section, "Pulse", "Core team", null);
        ReflectionTestUtils.setField(team, "id", 2L);

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
        ReflectionTestUtils.setField(student, "id", 3L);
        student.assignToTeam(team);

        when(teamRepository.findById(2L)).thenReturn(Optional.of(team));
        when(userAccountRepository.findById(3L)).thenReturn(Optional.of(student));
        when(userAccountRepository.findByAssignedTeamId(2L)).thenReturn(List.of());
        when(assignmentRepository.findByTeamId(2L)).thenReturn(List.of());

        TeamResponse response = teamService.removeStudent(2L, 3L);

        assertThat(student.getAssignedTeam()).isNull();
        assertThat(response.students()).isEmpty();
    }

    @Test
    void removeInstructorRejectsRemovingTheLastInstructor() {
        SeniorDesignSection section = new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                new Rubric("Rubric")
        );
        ReflectionTestUtils.setField(section, "id", 10L);
        SeniorDesignTeam team = new SeniorDesignTeam(section, "Pulse", "Core team", null);
        ReflectionTestUtils.setField(team, "id", 11L);

        UserAccount instructor = new UserAccount(
                "Ava",
                null,
                "Smith",
                "ava@tcu.edu",
                "{noop}",
                UserRole.INSTRUCTOR,
                UserStatus.ACTIVE,
                null
        );
        ReflectionTestUtils.setField(instructor, "id", 12L);

        when(teamRepository.findById(11L)).thenReturn(Optional.of(team));
        when(userAccountRepository.findById(12L)).thenReturn(Optional.of(instructor));
        when(assignmentRepository.existsByTeamIdAndInstructorId(11L, 12L)).thenReturn(true);
        when(assignmentRepository.findByTeamId(11L)).thenReturn(List.of(new TeamInstructorAssignment(team, instructor)));

        assertThatThrownBy(() -> teamService.removeInstructor(11L, 12L))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("at least one instructor");
    }

    @Test
    void deleteTeamRemovesAssignmentsAndAssociatedRecordsBeforePhysicalDelete() {
        SeniorDesignSection section = new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                new Rubric("Rubric")
        );
        ReflectionTestUtils.setField(section, "id", 21L);
        SeniorDesignTeam team = new SeniorDesignTeam(section, "Pulse", "Core team", null);
        ReflectionTestUtils.setField(team, "id", 22L);

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
        ReflectionTestUtils.setField(student, "id", 23L);
        student.assignToTeam(team);

        UserAccount instructor = new UserAccount(
                "Ava",
                null,
                "Smith",
                "ava@tcu.edu",
                "{noop}",
                UserRole.INSTRUCTOR,
                UserStatus.ACTIVE,
                null
        );
        ReflectionTestUtils.setField(instructor, "id", 24L);

        WeeklyActivity activity = org.mockito.Mockito.mock(WeeklyActivity.class);
        PeerEvaluationSubmission submission = org.mockito.Mockito.mock(PeerEvaluationSubmission.class);
        TeamInstructorAssignment assignment = new TeamInstructorAssignment(team, instructor);

        when(teamRepository.findById(22L)).thenReturn(Optional.of(team));
        when(userAccountRepository.findByAssignedTeamId(22L)).thenReturn(List.of(student));
        when(assignmentRepository.findByTeamId(22L)).thenReturn(List.of(assignment));
        when(weeklyActivityRepository.findByTeamId(22L)).thenReturn(List.of(activity));
        when(peerEvaluationSubmissionRepository.findByTeamId(22L)).thenReturn(List.of(submission));

        TeamDeletionResponse response = teamService.deleteTeam(22L);

        assertThat(student.getAssignedTeam()).isNull();
        assertThat(response.teamId()).isEqualTo(22L);
        assertThat(response.teamName()).isEqualTo("Pulse");
        assertThat(response.removedStudentAssignments()).isEqualTo(1);
        assertThat(response.removedInstructorAssignments()).isEqualTo(1);
        assertThat(response.deletedWarActivities()).isEqualTo(1);
        assertThat(response.deletedPeerEvaluationSubmissions()).isEqualTo(1);
        verify(assignmentRepository).deleteAll(List.of(assignment));
        verify(weeklyActivityRepository).deleteAll(List.of(activity));
        verify(peerEvaluationSubmissionRepository).deleteAll(List.of(submission));
        verify(teamRepository).delete(team);
    }
}
