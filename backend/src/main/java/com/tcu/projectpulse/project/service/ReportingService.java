package com.tcu.projectpulse.project.service;

import com.tcu.projectpulse.project.domain.PeerEvaluationItem;
import com.tcu.projectpulse.project.domain.PeerEvaluationSubmission;
import com.tcu.projectpulse.project.domain.WeeklyActivity;
import com.tcu.projectpulse.project.dto.CriterionAverage;
import com.tcu.projectpulse.project.dto.SectionPeerEvaluationReportResponse;
import com.tcu.projectpulse.project.dto.StudentPeerEvaluationReportResponse;
import com.tcu.projectpulse.project.dto.StudentWarReportResponse;
import com.tcu.projectpulse.project.dto.TeamWarReportResponse;
import com.tcu.projectpulse.project.dto.WeeklyActivityResponse;
import com.tcu.projectpulse.project.repository.PeerEvaluationItemRepository;
import com.tcu.projectpulse.project.repository.PeerEvaluationSubmissionRepository;
import com.tcu.projectpulse.project.repository.WeeklyActivityRepository;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportingService {

    private final WeeklyActivityRepository weeklyActivityRepository;
    private final PeerEvaluationSubmissionRepository submissionRepository;
    private final PeerEvaluationItemRepository itemRepository;
    private final UserAccountRepository userAccountRepository;
    private final TeamService teamService;
    private final SectionService sectionService;
    private final WarService warService;

    public ReportingService(WeeklyActivityRepository weeklyActivityRepository,
                            PeerEvaluationSubmissionRepository submissionRepository,
                            PeerEvaluationItemRepository itemRepository,
                            UserAccountRepository userAccountRepository,
                            TeamService teamService,
                            SectionService sectionService,
                            WarService warService) {
        this.weeklyActivityRepository = weeklyActivityRepository;
        this.submissionRepository = submissionRepository;
        this.itemRepository = itemRepository;
        this.userAccountRepository = userAccountRepository;
        this.teamService = teamService;
        this.sectionService = sectionService;
        this.warService = warService;
    }

    public TeamWarReportResponse getTeamWarReport(Long teamId, LocalDate weekStartDate) {
        var team = teamService.getTeamEntity(teamId);
        sectionService.getWeek(team.getSection().getId(), weekStartDate);

        List<UserAccount> students = userAccountRepository.findByAssignedTeamId(teamId).stream()
                .filter(user -> user.getRole() == UserRole.STUDENT)
                .sorted(Comparator.comparing(UserAccount::getLastName).thenComparing(UserAccount::getFirstName))
                .toList();
        List<WeeklyActivity> activities = weeklyActivityRepository
                .findByTeamIdAndActiveWeekWeekStartDateOrderByStudentLastNameAscStudentFirstNameAsc(teamId, weekStartDate);

        Map<Long, List<WeeklyActivityResponse>> groupedActivities = activities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getStudent().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(warService::toResponse, Collectors.toList())
                ));

        List<String> missingStudents = students.stream()
                .filter(student -> !groupedActivities.containsKey(student.getId()))
                .map(this::fullName)
                .toList();

        List<TeamWarReportResponse.StudentActivityGroup> studentGroups = students.stream()
                .filter(student -> groupedActivities.containsKey(student.getId()))
                .map(student -> new TeamWarReportResponse.StudentActivityGroup(
                        student.getId(),
                        fullName(student),
                        groupedActivities.get(student.getId())
                ))
                .toList();

        return new TeamWarReportResponse(team.getId(), team.getName(), weekStartDate, missingStudents, studentGroups);
    }

    public StudentWarReportResponse getStudentWarReport(Long studentId, LocalDate fromWeekStartDate, LocalDate toWeekStartDate) {
        UserAccount student = getStudent(studentId);
        LocalDate from = Objects.requireNonNullElse(fromWeekStartDate, student.getSection().getStartDate());
        LocalDate to = Objects.requireNonNullElse(toWeekStartDate, student.getSection().getEndDate());
        List<WeeklyActivity> activities = weeklyActivityRepository
                .findByStudentIdAndActiveWeekWeekStartDateBetweenOrderByActiveWeekWeekStartDateAsc(studentId, from, to);

        Map<LocalDate, List<WeeklyActivityResponse>> groupedByWeek = activities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getActiveWeek().getWeekStartDate(),
                        LinkedHashMap::new,
                        Collectors.mapping(warService::toResponse, Collectors.toList())
                ));

        return new StudentWarReportResponse(
                student.getId(),
                fullName(student),
                groupedByWeek.entrySet().stream()
                        .map(entry -> new StudentWarReportResponse.WeekActivities(entry.getKey(), entry.getValue()))
                        .toList()
        );
    }

    public SectionPeerEvaluationReportResponse getSectionPeerEvaluationReport(Long sectionId, LocalDate weekStartDate) {
        var section = sectionService.getSectionEntity(sectionId);
        sectionService.getWeek(sectionId, weekStartDate);

        List<UserAccount> sectionStudents = userAccountRepository
                .findBySectionIdAndRoleOrderByLastNameAscFirstNameAsc(sectionId, UserRole.STUDENT);
        List<PeerEvaluationSubmission> submissions = submissionRepository
                .findByTeamSectionIdAndActiveWeekWeekStartDate(sectionId, weekStartDate);

        Set<Long> authors = submissions.stream().map(submission -> submission.getAuthor().getId()).collect(Collectors.toSet());
        List<String> missingEvaluators = sectionStudents.stream()
                .filter(student -> !authors.contains(student.getId()))
                .map(this::fullName)
                .toList();

        Map<Long, List<PeerEvaluationItem>> itemsByEvaluatee = submissions.stream()
                .flatMap(submission -> submission.getItems().stream())
                .collect(Collectors.groupingBy(item -> item.getEvaluatee().getId()));

        List<SectionPeerEvaluationReportResponse.StudentPeerSummary> students = sectionStudents.stream()
                .filter(student -> itemsByEvaluatee.containsKey(student.getId()))
                .map(student -> toSectionSummary(student, itemsByEvaluatee.get(student.getId())))
                .toList();

        return new SectionPeerEvaluationReportResponse(
                section.getId(),
                section.getName(),
                weekStartDate,
                missingEvaluators,
                students
        );
    }

    public StudentPeerEvaluationReportResponse getStudentSelfPeerEvaluationReport(Long studentId, LocalDate weekStartDate) {
        UserAccount student = getStudent(studentId);
        List<PeerEvaluationItem> items = itemRepository.findByEvaluateeIdAndSubmissionActiveWeekWeekStartDate(studentId, weekStartDate);
        return new StudentPeerEvaluationReportResponse(
                student.getId(),
                fullName(student),
                items.isEmpty() ? List.of() : List.of(toStudentWeekReport(weekStartDate, items, false))
        );
    }

    public StudentPeerEvaluationReportResponse getInstructorStudentPeerEvaluationReport(Long studentId,
                                                                                        LocalDate fromWeekStartDate,
                                                                                        LocalDate toWeekStartDate) {
        UserAccount student = getStudent(studentId);
        LocalDate from = Objects.requireNonNullElse(fromWeekStartDate, student.getSection().getStartDate());
        LocalDate to = Objects.requireNonNullElse(toWeekStartDate, student.getSection().getEndDate());
        List<PeerEvaluationItem> items = itemRepository.findByEvaluateeIdAndSubmissionActiveWeekWeekStartDateBetween(studentId, from, to);
        Map<LocalDate, List<PeerEvaluationItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(item -> item.getSubmission().getActiveWeek().getWeekStartDate(), LinkedHashMap::new, Collectors.toList()));

        return new StudentPeerEvaluationReportResponse(
                student.getId(),
                fullName(student),
                grouped.entrySet().stream()
                        .map(entry -> toStudentWeekReport(entry.getKey(), entry.getValue(), true))
                        .toList()
        );
    }

    private SectionPeerEvaluationReportResponse.StudentPeerSummary toSectionSummary(UserAccount student,
                                                                                    List<PeerEvaluationItem> items) {
        BigDecimal averageGrade = average(items.stream()
                .map(this::totalScore)
                .toList());

        List<SectionPeerEvaluationReportResponse.CommentView> comments = items.stream()
                .map(item -> new SectionPeerEvaluationReportResponse.CommentView(
                        fullName(item.getSubmission().getAuthor()),
                        item.getPublicComment(),
                        item.getPrivateComment()
                ))
                .toList();

        List<SectionPeerEvaluationReportResponse.DetailedEvaluation> details = items.stream()
                .map(item -> new SectionPeerEvaluationReportResponse.DetailedEvaluation(
                        fullName(item.getSubmission().getAuthor()),
                        item.getCriterionScores().stream()
                                .sorted(Comparator.comparing(score -> score.getCriterion().getDisplayOrder()))
                                .map(score -> new CriterionAverage(
                                        score.getCriterion().getId(),
                                        score.getCriterion().getName(),
                                        BigDecimal.valueOf(score.getScore())
                                ))
                                .toList(),
                        item.getPublicComment(),
                        item.getPrivateComment()
                ))
                .toList();

        return new SectionPeerEvaluationReportResponse.StudentPeerSummary(
                student.getId(),
                fullName(student),
                averageGrade,
                comments,
                details
        );
    }

    private StudentPeerEvaluationReportResponse.WeekReport toStudentWeekReport(LocalDate weekStartDate,
                                                                               List<PeerEvaluationItem> items,
                                                                               boolean includeAllComments) {
        Map<Long, List<Integer>> criterionBuckets = new LinkedHashMap<>();
        Map<Long, String> criterionNames = new LinkedHashMap<>();
        for (PeerEvaluationItem item : items) {
            item.getCriterionScores().stream()
                    .sorted(Comparator.comparing(score -> score.getCriterion().getDisplayOrder()))
                    .forEach(score -> {
                        criterionBuckets.computeIfAbsent(score.getCriterion().getId(), ignored -> new java.util.ArrayList<>())
                                .add(score.getScore());
                        criterionNames.putIfAbsent(score.getCriterion().getId(), score.getCriterion().getName());
                    });
        }

        List<CriterionAverage> averages = criterionBuckets.entrySet().stream()
                .map(entry -> new CriterionAverage(
                        entry.getKey(),
                        criterionNames.get(entry.getKey()),
                        average(entry.getValue().stream().map(BigDecimal::valueOf).toList())
                ))
                .toList();

        List<String> publicComments = items.stream()
                .map(PeerEvaluationItem::getPublicComment)
                .filter(comment -> comment != null && !comment.isBlank())
                .map(comment -> includeAllComments ? comment : comment)
                .toList();

        return new StudentPeerEvaluationReportResponse.WeekReport(
                weekStartDate,
                average(items.stream().map(this::totalScore).toList()),
                averages,
                publicComments
        );
    }

    private UserAccount getStudent(Long studentId) {
        UserAccount user = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));
        if (user.getRole() != UserRole.STUDENT) {
            throw new InvalidArgumentException("User " + studentId + " is not a student");
        }
        return user;
    }

    private BigDecimal totalScore(PeerEvaluationItem item) {
        return item.getCriterionScores().stream()
                .map(score -> BigDecimal.valueOf(score.getScore()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private String fullName(UserAccount user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
