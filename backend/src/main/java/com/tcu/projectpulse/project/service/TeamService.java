package com.tcu.projectpulse.project.service;

import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.domain.SeniorDesignTeam;
import com.tcu.projectpulse.project.domain.TeamInstructorAssignment;
import com.tcu.projectpulse.project.dto.AssignInstructorsRequest;
import com.tcu.projectpulse.project.dto.AssignStudentsRequest;
import com.tcu.projectpulse.project.dto.CreateTeamRequest;
import com.tcu.projectpulse.project.dto.TeamDeletionResponse;
import com.tcu.projectpulse.project.dto.TeamResponse;
import com.tcu.projectpulse.project.dto.UpdateTeamRequest;
import com.tcu.projectpulse.project.repository.PeerEvaluationSubmissionRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignTeamRepository;
import com.tcu.projectpulse.project.repository.TeamInstructorAssignmentRepository;
import com.tcu.projectpulse.project.repository.WeeklyActivityRepository;
import com.tcu.projectpulse.shared.exception.ConflictException;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamService {

    private final SeniorDesignTeamRepository teamRepository;
    private final TeamInstructorAssignmentRepository assignmentRepository;
    private final WeeklyActivityRepository weeklyActivityRepository;
    private final PeerEvaluationSubmissionRepository peerEvaluationSubmissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final SectionService sectionService;

    public TeamService(SeniorDesignTeamRepository teamRepository,
                       TeamInstructorAssignmentRepository assignmentRepository,
                       WeeklyActivityRepository weeklyActivityRepository,
                       PeerEvaluationSubmissionRepository peerEvaluationSubmissionRepository,
                       UserAccountRepository userAccountRepository,
                       SectionService sectionService) {
        this.teamRepository = teamRepository;
        this.assignmentRepository = assignmentRepository;
        this.weeklyActivityRepository = weeklyActivityRepository;
        this.peerEvaluationSubmissionRepository = peerEvaluationSubmissionRepository;
        this.userAccountRepository = userAccountRepository;
        this.sectionService = sectionService;
    }

    public TeamResponse createTeam(CreateTeamRequest request) {
        if (teamRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Team name already exists: " + request.name());
        }
        SeniorDesignSection section = sectionService.getSectionEntity(request.sectionId());
        SeniorDesignTeam team = teamRepository.save(new SeniorDesignTeam(
                section,
                request.name(),
                request.description(),
                request.websiteUrl()
        ));
        return getTeam(team.getId());
    }

    public TeamResponse updateTeam(Long teamId, UpdateTeamRequest request) {
        SeniorDesignTeam team = getTeamEntity(teamId);
        teamRepository.findByNameIgnoreCase(request.name())
                .filter(existing -> !existing.getId().equals(teamId))
                .ifPresent(existing -> {
                    throw new ConflictException("Team name already exists: " + request.name());
                });
        team.update(request.name(), request.description(), request.websiteUrl());
        return getTeam(teamId);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> findTeams(Long sectionId) {
        List<SeniorDesignTeam> teams = sectionId == null
                ? teamRepository.findAll()
                : teamRepository.findBySectionIdOrderByNameAsc(sectionId);
        return teams.stream()
                .sorted(Comparator.comparing(SeniorDesignTeam::getName))
                .map(team -> getTeam(team.getId()))
                .toList();
    }

    public TeamResponse assignStudents(Long teamId, AssignStudentsRequest request) {
        SeniorDesignTeam team = getTeamEntity(teamId);
        for (Long studentId : request.studentIds()) {
            UserAccount student = getUser(studentId, UserRole.STUDENT);
            if (student.getStatus() != UserStatus.ACTIVE) {
                throw new InvalidArgumentException("Student must be active before assignment: " + studentId);
            }
            if (student.getSection() != null && !student.getSection().getId().equals(team.getSection().getId())) {
                throw new InvalidArgumentException("Student section does not match the target team section");
            }
            student.assignSection(team.getSection());
            student.assignToTeam(team);
        }
        return getTeam(teamId);
    }

    public TeamResponse assignInstructors(Long teamId, AssignInstructorsRequest request) {
        SeniorDesignTeam team = getTeamEntity(teamId);
        for (Long instructorId : request.instructorIds()) {
            UserAccount instructor = getUser(instructorId, UserRole.INSTRUCTOR);
            if (instructor.getStatus() != UserStatus.ACTIVE) {
                throw new InvalidArgumentException("Instructor must be active before assignment: " + instructorId);
            }
            if (!assignmentRepository.existsByTeamIdAndInstructorId(teamId, instructorId)) {
                assignmentRepository.save(new TeamInstructorAssignment(team, instructor));
            }
        }

        if (assignmentRepository.findByTeamId(teamId).isEmpty()) {
            throw new InvalidArgumentException("Every team must be assigned at least one instructor");
        }
        return getTeam(teamId);
    }

    public TeamResponse removeStudent(Long teamId, Long studentId) {
        getTeamEntity(teamId);
        UserAccount student = getUser(studentId, UserRole.STUDENT);
        if (student.getAssignedTeam() == null || !student.getAssignedTeam().getId().equals(teamId)) {
            throw new ResourceNotFoundException("Student " + studentId + " is not assigned to team " + teamId);
        }
        student.assignToTeam(null);
        return getTeam(teamId);
    }

    public TeamResponse removeInstructor(Long teamId, Long instructorId) {
        getTeamEntity(teamId);
        getUser(instructorId, UserRole.INSTRUCTOR);
        if (!assignmentRepository.existsByTeamIdAndInstructorId(teamId, instructorId)) {
            throw new ResourceNotFoundException("Instructor " + instructorId + " is not assigned to team " + teamId);
        }
        if (assignmentRepository.findByTeamId(teamId).size() <= 1) {
            throw new InvalidArgumentException("Every team must be assigned at least one instructor");
        }
        assignmentRepository.deleteByTeamIdAndInstructorId(teamId, instructorId);
        return getTeam(teamId);
    }

    public TeamDeletionResponse deleteTeam(Long teamId) {
        SeniorDesignTeam team = getTeamEntity(teamId);

        List<UserAccount> assignedStudents = userAccountRepository.findByAssignedTeamId(teamId);
        assignedStudents.forEach(student -> student.assignToTeam(null));

        List<TeamInstructorAssignment> instructorAssignments = assignmentRepository.findByTeamId(teamId);
        List<com.tcu.projectpulse.project.domain.WeeklyActivity> warActivities = weeklyActivityRepository.findByTeamId(teamId);
        List<com.tcu.projectpulse.project.domain.PeerEvaluationSubmission> peerEvaluationSubmissions =
                peerEvaluationSubmissionRepository.findByTeamId(teamId);

        assignmentRepository.deleteAll(instructorAssignments);
        weeklyActivityRepository.deleteAll(warActivities);
        peerEvaluationSubmissionRepository.deleteAll(peerEvaluationSubmissions);
        teamRepository.delete(team);

        return new TeamDeletionResponse(
                teamId,
                team.getName(),
                assignedStudents.size(),
                instructorAssignments.size(),
                warActivities.size(),
                peerEvaluationSubmissions.size()
        );
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeam(Long teamId) {
        SeniorDesignTeam team = getTeamEntity(teamId);
        return new TeamResponse(
                team.getId(),
                team.getSection().getName(),
                team.getName(),
                team.getDescription(),
                team.getWebsiteUrl(),
                userAccountRepository.findByAssignedTeamId(teamId).stream()
                        .filter(user -> user.getRole() == UserRole.STUDENT)
                        .sorted(Comparator.comparing(UserAccount::getLastName).thenComparing(UserAccount::getFirstName))
                        .map(user -> new TeamResponse.MemberView(user.getId(), fullName(user), user.getEmail()))
                        .toList(),
                assignmentRepository.findByTeamId(teamId).stream()
                        .map(TeamInstructorAssignment::getInstructor)
                        .sorted(Comparator.comparing(UserAccount::getLastName).thenComparing(UserAccount::getFirstName))
                        .map(user -> new TeamResponse.MemberView(user.getId(), fullName(user), user.getEmail()))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public SeniorDesignTeam getTeamEntity(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id " + teamId));
    }

    private UserAccount getUser(Long userId, UserRole expectedRole) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        if (user.getRole() != expectedRole) {
            throw new InvalidArgumentException("User " + userId + " is not a " + expectedRole);
        }
        return user;
    }

    private String fullName(UserAccount user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
