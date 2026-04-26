package com.tcu.projectpulse.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.auth.domain.AuthenticatedUser;
import com.tcu.projectpulse.auth.dto.LoginRequest;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.dto.UserAccountResponse;
import com.tcu.projectpulse.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private UserService userService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(authenticationManager, authenticatedUserService, userService);
    }

    @Test
    void loginStoresTheSecurityContextInTheHttpSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        LoginRequest loginRequest = new LoginRequest("admin@projectpulse.local", "Admin123!");
        AuthenticatedUser principal = new AuthenticatedUser(
                9L,
                "admin@projectpulse.local",
                "{bcrypt}hash",
                UserRole.ADMIN,
                UserStatus.ACTIVE
        );
        UsernamePasswordAuthenticationToken authenticated =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        UserAccountResponse accountResponse = new UserAccountResponse(
                9L,
                "Project",
                null,
                "Pulse Admin",
                "admin@projectpulse.local",
                UserRole.ADMIN,
                UserStatus.ACTIVE,
                null,
                null,
                null,
                null,
                java.util.List.of()
        );

        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticated);
        when(userService.getUser(9L)).thenReturn(accountResponse);

        UserAccountResponse response = authenticationService.login(loginRequest, request);

        assertThat(response).isEqualTo(accountResponse);
        assertThat(request.getSession(false)).isNotNull();
        assertThat(request.getSession(false).getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY))
                .isNotNull();
    }

    @Test
    void currentUserReturnsTheAuthenticatedUserProfile() {
        UserAccountResponse accountResponse = new UserAccountResponse(
                12L,
                "Jane",
                null,
                "Doe",
                "jane@tcu.edu",
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                3L,
                "2026-2027",
                8L,
                "Pulse",
                java.util.List.of()
        );

        when(authenticatedUserService.requireCurrentUserId()).thenReturn(12L);
        when(userService.getUser(12L)).thenReturn(accountResponse);

        assertThat(authenticationService.currentUser()).isEqualTo(accountResponse);
        verify(userService).getUser(12L);
    }
}
