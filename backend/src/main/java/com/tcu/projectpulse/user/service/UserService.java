package com.tcu.projectpulse.user.service;

import com.tcu.projectpulse.project.domain.TeamInstructorAssignment;
import com.tcu.projectpulse.project.repository.TeamInstructorAssignmentRepository;
import com.tcu.projectpulse.shared.exception.ConflictException;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.dto.UserAccountResponse;
import com.tcu.projectpulse.user.dto.InstructorDeactivationRequest;
import com.tcu.projectpulse.user.dto.UserProfileUpdateRequest;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final TeamInstructorAssignmentRepository teamInstructorAssignmentRepository;

    public UserService(UserAccountRepository userAccountRepository,
                       TeamInstructorAssignmentRepository teamInstructorAssignmentRepository) {
        this.userAccountRepository = userAccountRepository;
        this.teamInstructorAssignmentRepository = teamInstructorAssignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<UserAccountResponse> searchUsers(UserRole role, String name) {
        String normalizedName = name == null ? "" : name.toLowerCase(Locale.ROOT).trim();
        return userAccountRepository.findAll().stream()
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> normalizedName.isBlank() || fullName(user).toLowerCase(Locale.ROOT).contains(normalizedName))
                .sorted(Comparator.comparing(UserAccount::getLastName).thenComparing(UserAccount::getFirstName))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserAccountResponse getUser(Long userId) {
        return toResponse(findUser(userId));
    }

    public UserAccountResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        UserAccount user = findUser(userId);
        userAccountRepository.findByEmailIgnoreCase(request.email())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ConflictException("Email already in use: " + request.email());
                });
        user.updateProfile(request.firstName(), request.middleInitial(), request.lastName(), request.email());
        return toResponse(user);
    }

    public UserAccountResponse deactivateInstructor(Long userId, InstructorDeactivationRequest request) {
        UserAccount instructor = findInstructor(userId);
        if (instructor.getStatus() == UserStatus.DEACTIVATED) {
            throw new InvalidArgumentException("Instructor is already deactivated");
        }
        instructor.deactivate();
        return toResponse(instructor);
    }

    public UserAccountResponse reactivateInstructor(Long userId) {
        UserAccount instructor = findInstructor(userId);
        if (instructor.getStatus() == UserStatus.ACTIVE) {
            throw new InvalidArgumentException("Instructor is already active");
        }
        instructor.activate();
        return toResponse(instructor);
    }

    private UserAccount findUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
    }

    private UserAccount findInstructor(Long userId) {
        UserAccount user = findUser(userId);
        if (user.getRole() != UserRole.INSTRUCTOR) {
            throw new InvalidArgumentException("User " + userId + " is not an instructor");
        }
        return user;
    }

    private UserAccountResponse toResponse(UserAccount user) {
        List<String> supervisedTeams = user.getRole() == UserRole.INSTRUCTOR
                ? teamInstructorAssignmentRepository.findByInstructorId(user.getId()).stream()
                .map(TeamInstructorAssignment::getTeam)
                .map(team -> team.getName())
                .sorted()
                .toList()
                : List.of();

        return new UserAccountResponse(
                user.getId(),
                user.getFirstName(),
                user.getMiddleInitial(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getSection() == null ? null : user.getSection().getId(),
                user.getSection() == null ? null : user.getSection().getName(),
                user.getAssignedTeam() == null ? null : user.getAssignedTeam().getId(),
                user.getAssignedTeam() == null ? null : user.getAssignedTeam().getName(),
                supervisedTeams
        );
    }

    private String fullName(UserAccount user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
