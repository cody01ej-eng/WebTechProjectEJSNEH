package com.tcu.projectpulse.project.service;

import com.tcu.projectpulse.auth.service.AuthenticatedUserService;
import com.tcu.projectpulse.project.domain.ActiveWeek;
import com.tcu.projectpulse.project.dto.StudentWorkspaceContextResponse;
import com.tcu.projectpulse.project.repository.ActiveWeekRepository;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentWorkspaceService {

    private final AuthenticatedUserService authenticatedUserService;
    private final UserAccountRepository userAccountRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final Clock clock;

    public StudentWorkspaceService(AuthenticatedUserService authenticatedUserService,
                                   UserAccountRepository userAccountRepository,
                                   ActiveWeekRepository activeWeekRepository,
                                   Clock clock) {
        this.authenticatedUserService = authenticatedUserService;
        this.userAccountRepository = userAccountRepository;
        this.activeWeekRepository = activeWeekRepository;
        this.clock = clock;
    }

    public StudentWorkspaceContextResponse getCurrentStudentWorkspaceContext() {
        UserAccount student = authenticatedUserService.requireCurrentUserAccount();
        if (student.getRole() != UserRole.STUDENT) {
            throw new InvalidArgumentException("Current user is not a student");
        }

        Long sectionId = student.getSection() == null ? null : student.getSection().getId();
        Long teamId = student.getAssignedTeam() == null ? null : student.getAssignedTeam().getId();

        List<LocalDate> activeWeeks = sectionId == null
                ? List.of()
                : activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(sectionId).stream()
                        .filter(ActiveWeek::isActive)
                        .map(ActiveWeek::getWeekStartDate)
                        .toList();

        LocalDate currentWeekStartDate = LocalDate.now(clock)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate previousWeekStartDate = currentWeekStartDate.minusWeeks(1);

        List<StudentWorkspaceContextResponse.TeammateView> teammates = teamId == null
                ? List.of()
                : userAccountRepository.findByAssignedTeamId(teamId).stream()
                        .filter(user -> user.getRole() == UserRole.STUDENT)
                        .sorted(Comparator.comparing(UserAccount::getLastName).thenComparing(UserAccount::getFirstName))
                        .map(user -> new StudentWorkspaceContextResponse.TeammateView(
                                user.getId(),
                                fullName(user),
                                user.getId().equals(student.getId())
                        ))
                        .toList();

        List<StudentWorkspaceContextResponse.RubricCriterionView> rubricCriteria =
                student.getSection() == null || student.getSection().getRubric() == null
                        ? List.of()
                        : student.getSection().getRubric().getCriteria().stream()
                                .sorted(Comparator.comparing(criterion -> criterion.getDisplayOrder()))
                                .map(criterion -> new StudentWorkspaceContextResponse.RubricCriterionView(
                                        criterion.getId(),
                                        criterion.getName(),
                                        criterion.getDescription(),
                                        criterion.getMaxScore(),
                                        criterion.getDisplayOrder()
                                ))
                                .toList();

        return new StudentWorkspaceContextResponse(
                student.getId(),
                fullName(student),
                student.getEmail(),
                sectionId,
                student.getSection() == null ? null : student.getSection().getName(),
                teamId,
                student.getAssignedTeam() == null ? null : student.getAssignedTeam().getName(),
                student.getSection() == null || student.getSection().getRubric() == null ? null : student.getSection().getRubric().getId(),
                student.getSection() == null || student.getSection().getRubric() == null ? null : student.getSection().getRubric().getName(),
                resolveSuggestedWarWeek(activeWeeks, currentWeekStartDate),
                activeWeeks.contains(previousWeekStartDate) ? previousWeekStartDate : null,
                activeWeeks,
                teammates,
                rubricCriteria
        );
    }

    private LocalDate resolveSuggestedWarWeek(List<LocalDate> activeWeeks, LocalDate currentWeekStartDate) {
        if (activeWeeks.isEmpty()) {
            return currentWeekStartDate;
        }
        if (activeWeeks.contains(currentWeekStartDate)) {
            return currentWeekStartDate;
        }
        return activeWeeks.stream()
                .filter(week -> !week.isAfter(currentWeekStartDate))
                .reduce((previous, current) -> current)
                .orElse(activeWeeks.get(0));
    }

    private String fullName(UserAccount user) {
        StringBuilder fullName = new StringBuilder(user.getFirstName());
        if (user.getMiddleInitial() != null && !user.getMiddleInitial().isBlank()) {
            fullName.append(" ").append(user.getMiddleInitial().trim());
        }
        fullName.append(" ").append(user.getLastName());
        return fullName.toString().trim();
    }
}
