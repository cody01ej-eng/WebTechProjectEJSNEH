package com.tcu.projectpulse.auth.service;

import com.tcu.projectpulse.auth.domain.AuthenticatedUser;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticatedUserService {

    private final UserAccountRepository userAccountRepository;

    public AuthenticatedUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public Long requireCurrentUserId() {
        return currentPrincipal().getUserId();
    }

    public UserRole requireCurrentUserRole() {
        return currentPrincipal().getRole();
    }

    public UserAccount requireCurrentUserAccount() {
        Long userId = requireCurrentUserId();
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found with id " + userId));
    }

    private AuthenticatedUser currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user is available");
        }
        return principal;
    }
}
