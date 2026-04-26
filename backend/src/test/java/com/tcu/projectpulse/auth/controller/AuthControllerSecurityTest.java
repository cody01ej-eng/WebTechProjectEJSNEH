package com.tcu.projectpulse.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcu.projectpulse.auth.domain.InvitationStatus;
import com.tcu.projectpulse.auth.domain.InvitationType;
import com.tcu.projectpulse.auth.dto.InvitationBatchRequest;
import com.tcu.projectpulse.auth.dto.InvitationResponse;
import com.tcu.projectpulse.auth.service.AuthenticationService;
import com.tcu.projectpulse.auth.service.InvitationService;
import com.tcu.projectpulse.auth.service.RegistrationService;
import com.tcu.projectpulse.shared.api.StatusCode;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.dto.UserAccountResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.security.allowed-origins=http://localhost:5173")
@AutoConfigureMockMvc
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvitationService invitationService;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void inviteStudentsWithoutAuthenticationReturnsUnauthorized() throws Exception {
        InvitationBatchRequest request = new InvitationBatchRequest(
                1L,
                List.of("student.one@tcu.edu"),
                "Welcome to Project Pulse."
        );

        mockMvc.perform(post("/auth/invitations/students")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void csrfEndpointReturnsTokenWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.data.parameterName").value("_csrf"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void actuatorHealthEndpointReturnsOkWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void corsPreflightAllowsTheCsrfHeaderUsedByTheFrontend() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-csrf-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("x-csrf-token")));
    }

    @Test
    void loginWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@projectpulse.local",
                                  "password": "Admin123!"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("CSRF token missing or invalid"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void inviteStudentsAsStudentReturnsForbidden() throws Exception {
        InvitationBatchRequest request = new InvitationBatchRequest(
                1L,
                List.of("student.one@tcu.edu"),
                "Welcome to Project Pulse."
        );

        mockMvc.perform(post("/auth/invitations/students")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void inviteStudentsAsAdminReturnsCreatedInvitations() throws Exception {
        InvitationBatchRequest request = new InvitationBatchRequest(
                1L,
                List.of("student.one@tcu.edu"),
                "Welcome to Project Pulse."
        );
        when(invitationService.inviteStudents(any(InvitationBatchRequest.class))).thenReturn(List.of(
                new InvitationResponse(
                        21L,
                        "student-token",
                        "student.one@tcu.edu",
                        InvitationType.STUDENT,
                        InvitationStatus.PENDING,
                        "2026-2027",
                        LocalDateTime.of(2026, 5, 1, 12, 0),
                        "Welcome to Project Pulse."
                )
        ));

        mockMvc.perform(post("/auth/invitations/students")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data[0].email").value("student.one@tcu.edu"))
                .andExpect(jsonPath("$.data[0].type").value("STUDENT"));
    }

    @Test
    void loginWithInvalidPayloadReturnsReadableValidationErrors() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value(
                        "email: Email is required; password: Password must be between 8 and 255 characters"));
    }

    @Test
    void loginReturnsCurrentUserProfile() throws Exception {
        when(authenticationService.login(any(), any())).thenReturn(new UserAccountResponse(
                1L,
                "Project",
                null,
                "Admin",
                "admin@projectpulse.local",
                UserRole.ADMIN,
                UserStatus.ACTIVE,
                null,
                null,
                null,
                null,
                List.of()
        ));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@projectpulse.local",
                                  "password": "Admin123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.email").value("admin@projectpulse.local"));
    }

    @Test
    void currentUserWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void currentUserWithAuthenticationReturnsProfile() throws Exception {
        when(authenticationService.currentUser()).thenReturn(new UserAccountResponse(
                7L,
                "Jane",
                "Q",
                "Doe",
                "jane.doe@tcu.edu",
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                12L,
                "Senior Design A",
                19L,
                "Pulse Team",
                List.of()
        ));

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andExpect(jsonPath("$.data.assignedTeamName").value("Pulse Team"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void logoutWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("CSRF token missing or invalid"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void logoutWithCsrfTokenReturnsSuccess() throws Exception {
        mockMvc.perform(post("/auth/logout").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Logout success"));

        verify(authenticationService).logout(any(), any(), any(Authentication.class));
    }
}
