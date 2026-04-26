package com.tcu.projectpulse.auth.service;

import com.tcu.projectpulse.auth.domain.AuthenticatedUser;
import com.tcu.projectpulse.auth.dto.LoginRequest;
import com.tcu.projectpulse.user.dto.UserAccountResponse;
import com.tcu.projectpulse.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final AuthenticatedUserService authenticatedUserService;
    private final UserService userService;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 AuthenticatedUserService authenticatedUserService,
                                 UserService userService) {
        this.authenticationManager = authenticationManager;
        this.authenticatedUserService = authenticatedUserService;
        this.userService = userService;
    }

    public UserAccountResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password())
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        httpRequest.getSession(true)
                .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        return userService.getUser(principal.getUserId());
    }

    public UserAccountResponse currentUser() {
        return userService.getUser(authenticatedUserService.requireCurrentUserId());
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }
}
