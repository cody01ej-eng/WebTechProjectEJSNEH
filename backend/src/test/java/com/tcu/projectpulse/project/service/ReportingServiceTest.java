package com.tcu.projectpulse.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.project.domain.ActiveWeek;
import com.tcu.projectpulse.project.domain.PeerEvaluationCriterionScore;
import com.tcu.projectpulse.project.domain.PeerEvaluationItem;
import com.tcu.projectpulse.project.domain.PeerEvaluationSubmission;
import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.domain.RubricCriterion;
import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.domain.SeniorDesignTeam;
import com.tcu.projectpulse.project.dto.SectionPeerEvaluationReportResponse;
import com.tcu.projectpulse.project.repository.PeerEvaluationItemRepository;
import com.tcu.projectpulse.project.repository.PeerEvaluationSubmissionRepository;
import com.tcu.projectpulse.project.repository.WeeklyActivityRepository;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private WeeklyActivityRepository weeklyActivityRepository;

    @Mock
    private PeerEvaluationSubmissionRepository submissionRepository;

    @Mock
    private PeerEvaluationItemRepository itemRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private SectionService sectionService;

    @Mock
    private WarService warService;

    @InjectMocks
    private ReportingService reportingService;

    @Test
    void sectionPeerEvaluationReportCalculatesAverageGradePerStudent() {
        Rubric rubric = new Rubric("Rubric");
        ReflectionTestUtils.setField(rubric, "id", 1L);
        RubricCriterion quality = new RubricCriterion(rubric, "Quality", "Quality of work", BigDecimal.TEN, 1);
        RubricCriterion teamwork = new RubricCriterion(rubric, "Teamwork", "Teamwork", BigDecimal.TEN, 2);
        ReflectionTestUtils.setField(quality, "id", 11L);
        ReflectionTestUtils.setField(teamwork, "id", 12L);
        rubric.addCriterion(quality);
        rubric.addCriterion(teamwork);

        SeniorDesignSection section = new SeniorDesignSection("2025-2026", LocalDate.of(2025, 8, 25), LocalDate.of(2026, 5, 1), rubric);
        ReflectionTestUtils.setField(section, "id", 2L);
        SeniorDesignTeam team = new SeniorDesignTeam(section, "Pulse", "Team", null);
        ReflectionTestUtils.setField(team, "id", 3L);
        ActiveWeek week = new ActiveWeek(section, LocalDate.of(2026, 4, 13), true);
        ReflectionTestUtils.setField(week, "id", 4L);

        UserAccount authorOne = new UserAccount("Tim", null, "Smith", "tim@tcu.edu", "{noop}", UserRole.STUDENT, UserStatus.ACTIVE, section);
        UserAccount authorTwo = new UserAccount("Lily", null, "Fisher", "lily@tcu.edu", "{noop}", UserRole.STUDENT, UserStatus.ACTIVE, section);
        UserAccount evaluatee = new UserAccount("John", null, "Doe", "john@tcu.edu", "{noop}", UserRole.STUDENT, UserStatus.ACTIVE, section);
        ReflectionTestUtils.setField(authorOne, "id", 21L);
        ReflectionTestUtils.setField(authorTwo, "id", 22L);
        ReflectionTestUtils.setField(evaluatee, "id", 23L);
        authorOne.assignToTeam(team);
        authorTwo.assignToTeam(team);
        evaluatee.assignToTeam(team);

        PeerEvaluationSubmission submissionOne = new PeerEvaluationSubmission(authorOne, team, week);
        PeerEvaluationSubmission submissionTwo = new PeerEvaluationSubmission(authorTwo, team, week);

        PeerEvaluationItem itemOne = new PeerEvaluationItem(submissionOne, evaluatee, "Good work", "Private 1");
        itemOne.addCriterionScore(new PeerEvaluationCriterionScore(itemOne, quality, 10));
        itemOne.addCriterionScore(new PeerEvaluationCriterionScore(itemOne, teamwork, 8));
        submissionOne.addItem(itemOne);

        PeerEvaluationItem itemTwo = new PeerEvaluationItem(submissionTwo, evaluatee, "Needs more updates", "Private 2");
        itemTwo.addCriterionScore(new PeerEvaluationCriterionScore(itemTwo, quality, 6));
        itemTwo.addCriterionScore(new PeerEvaluationCriterionScore(itemTwo, teamwork, 6));
        submissionTwo.addItem(itemTwo);

        when(sectionService.getSectionEntity(2L)).thenReturn(section);
        when(sectionService.getWeek(2L, LocalDate.of(2026, 4, 13))).thenReturn(week);
        when(userAccountRepository.findBySectionIdAndRoleOrderByLastNameAscFirstNameAsc(2L, UserRole.STUDENT))
                .thenReturn(List.of(evaluatee, authorTwo, authorOne));
        when(submissionRepository.findByTeamSectionIdAndActiveWeekWeekStartDate(2L, LocalDate.of(2026, 4, 13)))
                .thenReturn(List.of(submissionOne, submissionTwo));

        SectionPeerEvaluationReportResponse response = reportingService.getSectionPeerEvaluationReport(2L, LocalDate.of(2026, 4, 13));

        assertThat(response.students()).hasSize(1);
        assertThat(response.students().get(0).studentName()).isEqualTo("John Doe");
        assertThat(response.students().get(0).averageGrade()).isEqualByComparingTo("15.00");
        assertThat(response.missingEvaluators()).contains("John Doe");
    }
}
