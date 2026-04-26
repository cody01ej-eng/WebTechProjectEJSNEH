package com.tcu.projectpulse.project.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tcu.projectpulse.project.domain.ActiveWeek;
import com.tcu.projectpulse.project.domain.ActivityCategory;
import com.tcu.projectpulse.project.domain.ActivityProgressStatus;
import com.tcu.projectpulse.project.domain.PeerEvaluationCriterionScore;
import com.tcu.projectpulse.project.domain.PeerEvaluationItem;
import com.tcu.projectpulse.project.domain.PeerEvaluationSubmission;
import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.domain.RubricCriterion;
import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.domain.SeniorDesignTeam;
import com.tcu.projectpulse.project.domain.WeeklyActivity;
import com.tcu.projectpulse.project.dto.SectionPeerEvaluationReportResponse;
import com.tcu.projectpulse.project.dto.TeamWarReportResponse;
import com.tcu.projectpulse.project.repository.ActiveWeekRepository;
import com.tcu.projectpulse.project.repository.PeerEvaluationSubmissionRepository;
import com.tcu.projectpulse.project.repository.RubricRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignSectionRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignTeamRepository;
import com.tcu.projectpulse.project.repository.WeeklyActivityRepository;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ActiveProfiles("test")
@Transactional
class ReportingServiceIntegrationTest {

    @Autowired
    private ReportingService reportingService;

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

    @Autowired
    private WeeklyActivityRepository weeklyActivityRepository;

    @Autowired
    private PeerEvaluationSubmissionRepository submissionRepository;

    @Test
    void getTeamWarReportGroupsPersistedActivitiesAndMissingStudents() {
        Rubric rubric = rubricRepository.save(new Rubric("WAR Report Rubric"));
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
        ActiveWeek week = activeWeekRepository.save(new ActiveWeek(section, LocalDate.of(2026, 4, 13), true));

        UserAccount bob = saveStudent("Bob", "Adams", section, team);
        UserAccount jane = saveStudent("Jane", "Doe", section, team);

        weeklyActivityRepository.save(new WeeklyActivity(
                jane,
                team,
                week,
                ActivityCategory.DEVELOPMENT,
                "Build reports",
                "Implemented the weekly reporting dashboard.",
                new BigDecimal("5.00"),
                new BigDecimal("4.50"),
                ActivityProgressStatus.IN_PROGRESS
        ));

        TeamWarReportResponse response = reportingService.getTeamWarReport(team.getId(), week.getWeekStartDate());

        assertThat(response.teamName()).isEqualTo("Pulse Team");
        assertThat(response.missingStudents()).containsExactly("Bob Adams");
        assertThat(response.students()).singleElement().satisfies(student -> {
            assertThat(student.studentId()).isEqualTo(jane.getId());
            assertThat(student.studentName()).isEqualTo("Jane Doe");
            assertThat(student.activities()).extracting(activity -> activity.plannedActivity())
                    .containsExactly("Build reports");
        });
    }

    @Test
    void getSectionPeerEvaluationReportCalculatesAggregateScoresFromPersistedSubmissions() {
        Rubric rubric = new Rubric("Peer Evaluation Rubric");
        RubricCriterion quality = new RubricCriterion(rubric, "Quality", "Quality of work", BigDecimal.TEN, 1);
        RubricCriterion teamwork = new RubricCriterion(rubric, "Teamwork", "Team collaboration", BigDecimal.TEN, 2);
        rubric.addCriterion(quality);
        rubric.addCriterion(teamwork);
        rubric = rubricRepository.save(rubric);

        SeniorDesignSection section = sectionRepository.save(new SeniorDesignSection(
                "2027-2028",
                LocalDate.of(2027, 8, 23),
                LocalDate.of(2028, 5, 1),
                rubric
        ));
        SeniorDesignTeam team = teamRepository.save(new SeniorDesignTeam(section, "Pulse Team", "Core product team", null));
        ActiveWeek week = activeWeekRepository.save(new ActiveWeek(section, LocalDate.of(2028, 4, 10), true));

        UserAccount john = saveStudent("John", "Doe", section, team);
        UserAccount lily = saveStudent("Lily", "Fisher", section, team);
        UserAccount tim = saveStudent("Tim", "Smith", section, team);

        PeerEvaluationSubmission submissionOne = new PeerEvaluationSubmission(lily, team, week);
        PeerEvaluationItem itemOne = new PeerEvaluationItem(submissionOne, john, "Good work", "Private 1");
        itemOne.addCriterionScore(new PeerEvaluationCriterionScore(itemOne, quality, 10));
        itemOne.addCriterionScore(new PeerEvaluationCriterionScore(itemOne, teamwork, 8));
        submissionOne.addItem(itemOne);

        PeerEvaluationSubmission submissionTwo = new PeerEvaluationSubmission(tim, team, week);
        PeerEvaluationItem itemTwo = new PeerEvaluationItem(submissionTwo, john, "Needs more updates", "Private 2");
        itemTwo.addCriterionScore(new PeerEvaluationCriterionScore(itemTwo, quality, 6));
        itemTwo.addCriterionScore(new PeerEvaluationCriterionScore(itemTwo, teamwork, 6));
        submissionTwo.addItem(itemTwo);

        submissionRepository.saveAll(List.of(submissionOne, submissionTwo));

        SectionPeerEvaluationReportResponse response = reportingService.getSectionPeerEvaluationReport(
                section.getId(),
                week.getWeekStartDate()
        );

        assertThat(response.missingEvaluators()).containsExactly("John Doe");
        assertThat(response.students()).singleElement().satisfies(student -> {
            assertThat(student.studentName()).isEqualTo("John Doe");
            assertThat(student.averageGrade()).isEqualByComparingTo("15.00");
            assertThat(student.comments()).extracting(SectionPeerEvaluationReportResponse.CommentView::publicComment)
                    .containsExactlyInAnyOrder("Good work", "Needs more updates");
            assertThat(student.details()).extracting(SectionPeerEvaluationReportResponse.DetailedEvaluation::evaluatorName)
                    .containsExactlyInAnyOrder("Lily Fisher", "Tim Smith");
        });
    }

    private UserAccount saveStudent(String firstName,
                                    String lastName,
                                    SeniorDesignSection section,
                                    SeniorDesignTeam team) {
        UserAccount student = new UserAccount(
                firstName,
                null,
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
}
