package com.tcu.projectpulse.project.service;

import com.tcu.projectpulse.auth.service.AuthenticatedUserService;
import com.tcu.projectpulse.project.domain.ActiveWeek;
import com.tcu.projectpulse.project.domain.WeeklyActivity;
import com.tcu.projectpulse.project.dto.WarActivityRequest;
import com.tcu.projectpulse.project.dto.WeeklyActivityResponse;
import com.tcu.projectpulse.project.repository.WeeklyActivityRepository;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WarService {

    private final WeeklyActivityRepository weeklyActivityRepository;
    private final UserAccountRepository userAccountRepository;
    private final SectionService sectionService;
    private final Clock clock;
    private final AuthenticatedUserService authenticatedUserService;

    public WarService(WeeklyActivityRepository weeklyActivityRepository,
                      UserAccountRepository userAccountRepository,
                      SectionService sectionService,
                      Clock clock,
                      AuthenticatedUserService authenticatedUserService) {
        this.weeklyActivityRepository = weeklyActivityRepository;
        this.userAccountRepository = userAccountRepository;
        this.sectionService = sectionService;
        this.clock = clock;
        this.authenticatedUserService = authenticatedUserService;
    }

    public WeeklyActivityResponse createActivity(WarActivityRequest request) {
        verifyCurrentStudentOwnsResource(request.studentId());
        UserAccount student = getStudent(request.studentId());
        ActiveWeek week = resolveWeek(student, request.weekStartDate());

        WeeklyActivity activity = weeklyActivityRepository.save(new WeeklyActivity(
                student,
                student.getAssignedTeam(),
                week,
                request.category(),
                request.plannedActivity(),
                request.description(),
                request.plannedHours(),
                request.actualHours(),
                request.status()
        ));

        return toResponse(activity);
    }

    public WeeklyActivityResponse updateActivity(Long activityId, WarActivityRequest request) {
        verifyCurrentStudentOwnsResource(request.studentId());
        WeeklyActivity activity = weeklyActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WAR activity not found with id " + activityId));
        if (!activity.getStudent().getId().equals(authenticatedUserService.requireCurrentUserId())) {
            throw new AccessDeniedException("Students can only update their own WAR activities");
        }
        if (!activity.getStudent().getId().equals(request.studentId())) {
            throw new InvalidArgumentException("WAR activity can only be updated by the owning student");
        }
        if (!activity.getActiveWeek().getWeekStartDate().equals(request.weekStartDate())) {
            throw new InvalidArgumentException("WAR activity week cannot be changed");
        }
        activity.update(
                request.category(),
                request.plannedActivity(),
                request.description(),
                request.plannedHours(),
                request.actualHours(),
                request.status()
        );
        return toResponse(activity);
    }

    public void deleteActivity(Long activityId) {
        WeeklyActivity activity = weeklyActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WAR activity not found with id " + activityId));
        if (!activity.getStudent().getId().equals(authenticatedUserService.requireCurrentUserId())) {
            throw new AccessDeniedException("Students can only delete their own WAR activities");
        }
        weeklyActivityRepository.delete(activity);
    }

    public WeeklyActivityResponse toResponse(WeeklyActivity activity) {
        return new WeeklyActivityResponse(
                activity.getId(),
                activity.getStudent().getId(),
                fullName(activity.getStudent()),
                activity.getTeam().getId(),
                activity.getTeam().getName(),
                activity.getActiveWeek().getWeekStartDate(),
                activity.getCategory(),
                activity.getPlannedActivity(),
                activity.getDescription(),
                activity.getPlannedHours(),
                activity.getActualHours(),
                activity.getStatus()
        );
    }

    private UserAccount getStudent(Long studentId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));
        if (student.getRole() != UserRole.STUDENT) {
            throw new InvalidArgumentException("User " + studentId + " is not a student");
        }
        if (student.getAssignedTeam() == null || student.getSection() == null) {
            throw new InvalidArgumentException("Student must be assigned to a team before managing WAR activities");
        }
        return student;
    }

    private ActiveWeek resolveWeek(UserAccount student, LocalDate weekStartDate) {
        LocalDate currentWeekStart = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (weekStartDate.isAfter(currentWeekStart)) {
            throw new InvalidArgumentException("Students cannot create WAR activities for future weeks");
        }
        return sectionService.getWeek(student.getSection().getId(), weekStartDate);
    }

    private String fullName(UserAccount user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }

    private void verifyCurrentStudentOwnsResource(Long studentId) {
        if (!authenticatedUserService.requireCurrentUserId().equals(studentId)) {
            throw new AccessDeniedException("Students can only manage their own WAR activities");
        }
    }
}
