package com.tcu.projectpulse.auth.controller;

import com.tcu.projectpulse.auth.dto.InstructorRegistrationRequest;
import com.tcu.projectpulse.auth.dto.InvitationBatchRequest;
import com.tcu.projectpulse.auth.dto.InvitationResponse;
import com.tcu.projectpulse.auth.dto.LoginRequest;
import com.tcu.projectpulse.auth.dto.StudentRegistrationRequest;
import com.tcu.projectpulse.auth.dto.CsrfTokenResponse;
import com.tcu.projectpulse.auth.service.AuthenticationService;
import com.tcu.projectpulse.auth.service.InvitationService;
import com.tcu.projectpulse.auth.service.RegistrationService;
import com.tcu.projectpulse.shared.api.Result;
import com.tcu.projectpulse.user.dto.UserAccountResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final InvitationService invitationService;
    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    public AuthController(InvitationService invitationService,
                          RegistrationService registrationService,
                          AuthenticationService authenticationService) {
        this.invitationService = invitationService;
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/invitations/students")
    public Result<List<InvitationResponse>> inviteStudents(@Valid @RequestBody InvitationBatchRequest request) {
        return Result.success("Student invitations processed", invitationService.inviteStudents(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/invitations/instructors")
    public Result<List<InvitationResponse>> inviteInstructors(@Valid @RequestBody InvitationBatchRequest request) {
        return Result.success("Instructor invitations processed", invitationService.inviteInstructors(request));
    }

    @PostMapping("/registrations/students")
    public Result<UserAccountResponse> registerStudent(@Valid @RequestBody StudentRegistrationRequest request) {
        return Result.success("Student registration success", registrationService.registerStudent(request));
    }

    @PostMapping("/registrations/instructors")
    public Result<UserAccountResponse> registerInstructor(@Valid @RequestBody InstructorRegistrationRequest request) {
        return Result.success("Instructor registration success", registrationService.registerInstructor(request));
    }

    @PostMapping("/login")
    public Result<UserAccountResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.success("Login success", authenticationService.login(request, httpRequest));
    }

    @GetMapping("/csrf")
    public Result<CsrfTokenResponse> csrfToken(CsrfToken csrfToken) {
        return Result.success("CSRF token loaded", new CsrfTokenResponse(
                csrfToken.getHeaderName(),
                csrfToken.getParameterName(),
                csrfToken.getToken()
        ));
    }

    @GetMapping("/me")
    public Result<UserAccountResponse> currentUser() {
        return Result.success("Current user loaded", authenticationService.currentUser());
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request,
                               HttpServletResponse response,
                               Authentication authentication) {
        authenticationService.logout(request, response, authentication);
        return Result.success("Logout success");
    }
}
