package com.tcu.projectpulse.auth.service;

import com.tcu.projectpulse.user.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("authorizationService")
@Transactional(readOnly = true)
public class AuthorizationService {

    private final AuthenticatedUserService authenticatedUserService;

    public AuthorizationService(AuthenticatedUserService authenticatedUserService) {
        this.authenticatedUserService = authenticatedUserService;
    }

    public boolean isCurrentUser(Long userId) {
        return authenticatedUserService.requireCurrentUserId().equals(userId);
    }

    public boolean canViewUser(Long userId) {
        UserRole role = authenticatedUserService.requireCurrentUserRole();
        return role == UserRole.ADMIN || role == UserRole.INSTRUCTOR || isCurrentUser(userId);
    }

    public boolean canUpdateUser(Long userId) {
        UserRole role = authenticatedUserService.requireCurrentUserRole();
        return role == UserRole.ADMIN || isCurrentUser(userId);
    }

    public boolean canAccessStudentSelfReport(Long studentId) {
        return authenticatedUserService.requireCurrentUserRole() == UserRole.STUDENT && isCurrentUser(studentId);
    }

    public boolean canAccessTeamWarReport(Long teamId) {
        UserRole role = authenticatedUserService.requireCurrentUserRole();
        if (role == UserRole.ADMIN || role == UserRole.INSTRUCTOR) {
            return true;
        }
        if (role != UserRole.STUDENT) {
            return false;
        }
        var user = authenticatedUserService.requireCurrentUserAccount();
        return user.getAssignedTeam() != null && user.getAssignedTeam().getId().equals(teamId);
    }
}
