package com.tcu.projectpulse.project.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.auth.service.AuthenticatedUserService;
import com.tcu.projectpulse.project.domain.ActiveWeek;
import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.domain.RubricCriterion;
import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.domain.SeniorDesignTeam;
import com.tcu.projectpulse.project.dto.SubmitPeerEvaluationRequest;
import com.tcu.projectpulse.project.repository.PeerEvaluationSubmissionRepository;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
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
class PeerEvaluationServiceTest {

    @Mock
    private PeerEvaluationSubmissionRepository submissionRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private SectionService sectionService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    private PeerEvaluationService peerEvaluationService;

    @BeforeEach
    void setUp() {
        peerEvaluationService = new PeerEvaluationService(
                submissionRepository,
                userAccountRepository,
                sectionService,
                Clock.fixed(Instant.parse("2026-04-22T12:00:00Z"), ZoneId.of("America/Chicago")),
                authenticatedUserService
        );
    }

    @Test
    void submitEvaluationRejectsWeeksOtherThanThePreviousWeek() {
        Rubric rubric = new Rubric("Core Rubric");
        ReflectionTestUtils.setField(rubric, "id", 1L);
        RubricCriterion criterion = new RubricCriterion(rubric, "Quality", "Quality of work", BigDecimal.TEN, 1);
        ReflectionTestUtils.setField(criterion, "id", 3L);
        rubric.addCriterion(criterion);

        SeniorDesignSection section = new SeniorDesignSection("2025-2026", LocalDate.of(2025, 8, 25), LocalDate.of(2026, 5, 1), rubric);
        ReflectionTestUtils.setField(section, "id", 4L);
        SeniorDesignTeam team = new SeniorDesignTeam(section, "Pulse", "Core team", null);
        ReflectionTestUtils.setField(team, "id", 5L);

        UserAccount student = new UserAccount("Jane", null, "Doe", "jane@tcu.edu", "{noop}password", UserRole.STUDENT, UserStatus.ACTIVE, section);
        ReflectionTestUtils.setField(student, "id", 6L);
        student.assignToTeam(team);

        when(authenticatedUserService.requireCurrentUserId()).thenReturn(6L);
        when(userAccountRepository.findById(6L)).thenReturn(java.util.Optional.of(student));
        SubmitPeerEvaluationRequest request = new SubmitPeerEvaluationRequest(
                6L,
                LocalDate.of(2026, 4, 6),
                List.of()
        );

        assertThatThrownBy(() -> peerEvaluationService.submitEvaluation(request))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("previous week");
    }
}
