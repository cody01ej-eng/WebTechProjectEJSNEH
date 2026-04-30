package com.tcu.projectpulse.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcu.projectpulse.auth.service.AuthorizationService;
import com.tcu.projectpulse.shared.api.StatusCode;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.dto.StudentDeletionResponse;
import com.tcu.projectpulse.user.dto.UserAccountResponse;
import com.tcu.projectpulse.user.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean(name = "authorizationService")
    private AuthorizationService authorizationService;

    // ─── GET /users ─────────────────────────────────────────────────────────────

    @Test
    void searchUsersWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void searchUsersAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsersAsAdminReturnsData() throws Exception {
        when(userService.searchUsers(null, null)).thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data[0].id").value(5));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void searchUsersAsInstructorReturnsData() throws Exception {
        when(userService.searchUsers(null, null)).thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data[0].id").value(5));
    }

    // ─── GET /users/{userId} ─────────────────────────────────────────────────────

    @Test
    void getUserWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/users/5"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getUserWhenAuthorizationDeniedReturnsForbidden() throws Exception {
        when(authorizationService.canViewUser(5L)).thenReturn(false);

        mockMvc.perform(get("/users/5"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserWhenAuthorizedReturnsData() throws Exception {
        when(authorizationService.canViewUser(5L)).thenReturn(true);
        when(userService.getUser(5L)).thenReturn(sampleUser());

        mockMvc.perform(get("/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.id").value(5));
    }

    // ─── PUT /users/{userId} ─────────────────────────────────────────────────────

    @Test
    void updateUserWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(put("/users/5")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validProfileUpdateRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void updateUserWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(put("/users/5")
                        .contentType(APPLICATION_JSON)
                        .content(validProfileUpdateRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("CSRF token missing or invalid"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void updateUserWhenAuthorizationDeniedReturnsForbidden() throws Exception {
        when(authorizationService.canUpdateUser(5L)).thenReturn(false);

        mockMvc.perform(put("/users/5")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validProfileUpdateRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void updateUserWhenAuthorizedReturnsData() throws Exception {
        when(authorizationService.canUpdateUser(5L)).thenReturn(true);
        when(userService.updateProfile(eq(5L), any())).thenReturn(sampleUser());

        mockMvc.perform(put("/users/5")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validProfileUpdateRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.id").value(5));
    }

    // ─── POST /users/{userId}/deactivate ─────────────────────────────────────────

    @Test
    void deactivateInstructorWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/users/5/deactivate")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validDeactivationRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void deactivateInstructorWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/users/5/deactivate")
                        .contentType(APPLICATION_JSON)
                        .content(validDeactivationRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("CSRF token missing or invalid"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void deactivateInstructorAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(post("/users/5/deactivate")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validDeactivationRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void deactivateInstructorAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(post("/users/5/deactivate")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validDeactivationRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateInstructorAsAdminReturnsData() throws Exception {
        when(userService.deactivateInstructor(eq(5L), any())).thenReturn(sampleUser());

        mockMvc.perform(post("/users/5/deactivate")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validDeactivationRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.id").value(5));
    }

    // ─── POST /users/{userId}/reactivate ─────────────────────────────────────────

    @Test
    void reactivateInstructorWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/users/5/reactivate").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void reactivateInstructorAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(post("/users/5/reactivate").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void reactivateInstructorAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(post("/users/5/reactivate").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reactivateInstructorAsAdminReturnsData() throws Exception {
        when(userService.reactivateInstructor(5L)).thenReturn(sampleUser());

        mockMvc.perform(post("/users/5/reactivate").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    void deleteStudentWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/users/7").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void deleteStudentAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/users/7").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteStudentAsAdminReturnsDeletionSummary() throws Exception {
        when(userService.deleteStudent(7L)).thenReturn(new StudentDeletionResponse(7L, "Jane Doe", 4, 2, 3));

        mockMvc.perform(delete("/users/7").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.studentId").value(7))
                .andExpect(jsonPath("$.data.studentName").value("Jane Doe"))
                .andExpect(jsonPath("$.data.deletedPeerEvaluationItems").value(3));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private UserAccountResponse sampleUser() {
        return new UserAccountResponse(5L, "Alice", null, "Smith", "alice@example.com",
                UserRole.INSTRUCTOR, UserStatus.ACTIVE, null, null, null, null, List.of());
    }

    private String validProfileUpdateRequest() throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "firstName", "Alice",
                "middleInitial", "B",
                "lastName", "Smith",
                "email", "alice@example.com"
        ));
    }

    private String validDeactivationRequest() throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "reason", "Violation of academic conduct policy"
        ));
    }
}
