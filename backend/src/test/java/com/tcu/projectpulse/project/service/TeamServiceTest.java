package com.tcu.projectpulse.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.domain.SeniorDesignTeam;
import com.tcu.projectpulse.project.domain.TeamInstructorAssignment;
import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.dto.TeamResponse;
import com.tcu.projectpulse.project.repository.SeniorDesignTeamRepository;
import com.tcu.projectpulse.project.repository.TeamInstructorAssignmentRepository;
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
}
