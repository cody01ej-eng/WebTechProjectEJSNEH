package com.tcu.projectpulse.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcu.projectpulse.project.dto.PeerEvaluationSubmissionResponse;
import com.tcu.projectpulse.project.service.PeerEvaluationService;
import com.tcu.projectpulse.shared.api.StatusCode;
import java.time.LocalDate;
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
class PeerEvaluationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PeerEvaluationService peerEvaluationService;

    // ─── POST /project/peer-evaluations ──────────────────────────────────────────

    @Test
    void submitEvaluationWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/project/peer-evaluations")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validEvaluationRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void submitEvaluationWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/project/peer-evaluations")
                        .contentType(APPLICATION_JSON)
                        .content(validEvaluationRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("CSRF token missing or invalid"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void submitEvaluationAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(post("/project/peer-evaluations")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validEvaluationRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void submitEvaluationAsAdminReturnsForbidden() throws Exception {
        mockMvc.perform(post("/project/peer-evaluations")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validEvaluationRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void submitEvaluationAsStudentReturnsData() throws Exception {
        when(peerEvaluationService.submitEvaluation(any())).thenReturn(
                new PeerEvaluationSubmissionResponse(
                        101L, 7L, "Jane Doe", 19L, "Pulse Team",
                        LocalDate.of(2026, 4, 20), List.of()
                )
        );

        mockMvc.perform(post("/project/peer-evaluations")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validEvaluationRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.submissionId").value(101))
                .andExpect(jsonPath("$.data.authorId").value(7));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private String validEvaluationRequest() throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "authorId", 7,
                "weekStartDate", "2026-04-20",
                "evaluations", List.of(
                        java.util.Map.of(
                                "evaluateeId", 8,
                                "publicComment", "Great work",
                                "privateComment", "Could improve documentation",
                                "scores", List.of(
                                        java.util.Map.of("criterionId", 1, "score", 4)
                                )
                        )
                )
        ));
    }
}
