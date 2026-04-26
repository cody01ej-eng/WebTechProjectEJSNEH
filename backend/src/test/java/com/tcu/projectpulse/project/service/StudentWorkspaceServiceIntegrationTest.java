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
import com.tcu.projectpulse.project.repository.RubricRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignSectionRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignTeamRepository;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ActiveProfiles("test")
@Transactional
@Import(StudentWorkspaceServiceIntegrationTest.FixedClockConfig.class)
class StudentWorkspaceServiceIntegrationTest {

    @Autowired
    private StudentWorkspaceService studentWorkspaceService;

    @Autowired
    private RubricRepository rubricRepository;

    @Autowired
    private SeniorDesignSectionRepository sectionRepository;

    @Autowired
    private SeniorDesignTeamRepository teamRepository;

    @Autowired
    private ActiveWeekRepository activeWeekRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @MockitoBean
    private AuthenticatedUserService authenticatedUserService;

    @Test
    void getCurrentStudentWorkspaceContextBuildsGuidedContextFromPersistedData() {
        Rubric rubric = new Rubric("Peer Evaluation Rubric");
        rubric.addCriterion(new RubricCriterion(
                rubric,
                "Initiative",
                "Rate the teammate's initiative.",
                BigDecimal.TEN,
                2
        ));
        rubric.addCriterion(new RubricCriterion(
                rubric,
                "Quality of work",
                "Rate the quality of this teammate's work.",
                BigDecimal.TEN,
                1
        ));
        rubric = rubricRepository.save(rubric);

        SeniorDesignSection section = sectionRepository.save(new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                rubric
        ));
        SeniorDesignTeam team = teamRepository.save(new SeniorDesignTeam(
                section,
                "Pulse Team",
                "Core product team",
                "https://pulse.example"
        ));

        UserAccount currentStudent = saveStudent("Jane", "Q", "Doe", section, team);
        saveStudent("Lily", null, "Fisher", section, team);
        saveStudent("Amy", null, "Adams", section, team);

        activeWeekRepository.saveAll(List.of(
                new ActiveWeek(section, LocalDate.of(2026, 4, 20), false),
                new ActiveWeek(section, LocalDate.of(2026, 4, 27), true),
                new ActiveWeek(section, LocalDate.of(2026, 4, 13), true),
                new ActiveWeek(section, LocalDate.of(2026, 4, 6), true)
        ));

        when(authenticatedUserService.requireCurrentUserAccount()).thenReturn(currentStudent);

        StudentWorkspaceContextResponse response = studentWorkspaceService.getCurrentStudentWorkspaceContext();

        assertThat(response.studentName()).isEqualTo("Jane Q Doe");
        assertThat(response.sectionName()).isEqualTo("2026-2027");
        assertThat(response.teamName()).isEqualTo("Pulse Team");
        assertThat(response.suggestedWarWeekStartDate()).isEqualTo(LocalDate.of(2026, 4, 13));
        assertThat(response.suggestedPeerEvaluationWeekStartDate()).isEqualTo(LocalDate.of(2026, 4, 13));
        assertThat(response.activeWeeks()).containsExactly(
                LocalDate.of(2026, 4, 6),
                LocalDate.of(2026, 4, 13),
                LocalDate.of(2026, 4, 27)
        );
        assertThat(response.teammates()).extracting(StudentWorkspaceContextResponse.TeammateView::name)
                .containsExactly("Amy Adams", "Jane Q Doe", "Lily Fisher");
        assertThat(response.teammates()).extracting(StudentWorkspaceContextResponse.TeammateView::self)
                .containsExactly(false, true, false);
        assertThat(response.rubricCriteria()).extracting(StudentWorkspaceContextResponse.RubricCriterionView::name)
                .containsExactly("Quality of work", "Initiative");
    }

    private UserAccount saveStudent(String firstName,
                                    String middleInitial,
                                    String lastName,
                                    SeniorDesignSection section,
                                    SeniorDesignTeam team) {
        UserAccount student = new UserAccount(
                firstName,
                middleInitial,
                lastName,
                firstName.toLowerCase() + "." + lastName.toLowerCase() + "@tcu.edu",
                "{noop}password",
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                section
        );
        student.assignToTeam(team);
        return userAccountRepository.save(student);
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-04-22T12:00:00Z"), ZoneId.of("America/Chicago"));
        }
    }
}
