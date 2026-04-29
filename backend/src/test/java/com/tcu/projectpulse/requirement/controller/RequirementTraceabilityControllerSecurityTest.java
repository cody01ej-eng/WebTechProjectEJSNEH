package com.tcu.projectpulse.requirement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcu.projectpulse.requirement.domain.ComponentType;
import com.tcu.projectpulse.requirement.domain.RequirementSource;
import com.tcu.projectpulse.requirement.domain.TraceabilityStatus;
import com.tcu.projectpulse.requirement.dto.CreateRequirementReferenceRequest;
import com.tcu.projectpulse.requirement.dto.CreateTraceabilityLinkRequest;
import com.tcu.projectpulse.requirement.dto.RequirementReferenceResponse;
import com.tcu.projectpulse.requirement.dto.TraceabilityLinkResponse;
import com.tcu.projectpulse.requirement.service.RequirementTraceabilityService;
import com.tcu.projectpulse.shared.api.StatusCode;
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
class RequirementTraceabilityControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RequirementTraceabilityService requirementTraceabilityService;

    private RequirementReferenceResponse sampleReference() {
        return new RequirementReferenceResponse(1L, "UC-01", "User Login", RequirementSource.USE_CASE, "User can log in");
    }

    private TraceabilityLinkResponse sampleLink() {
        return new TraceabilityLinkResponse(1L, 1L, "UC-01", "User Login", ComponentType.BACKEND_CONTROLLER, "AuthController", TraceabilityStatus.IMPLEMENTED, "Handles login");
    }

    // ─── GET /requirements/references ────────────────────────────────────────────

    @Test
    void getRequirementsWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/requirements/references"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getRequirementsAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(get("/requirements/references"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getRequirementsAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(get("/requirements/references"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRequirementsAsAdminReturnsData() throws Exception {
        when(requirementTraceabilityService.findRequirements()).thenReturn(List.of(sampleReference()));

        mockMvc.perform(get("/requirements/references"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find requirements success"));
    }

    // ─── POST /requirements/references ───────────────────────────────────────────

    @Test
    void createRequirementWithoutAuthenticationReturnsUnauthorized() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateRequirementReferenceRequest("UC-01", "User Login", RequirementSource.USE_CASE, "User can log in"));

        mockMvc.perform(post("/requirements/references").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createRequirementAsStudentReturnsForbidden() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateRequirementReferenceRequest("UC-01", "User Login", RequirementSource.USE_CASE, "User can log in"));

        mockMvc.perform(post("/requirements/references").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createRequirementAsInstructorReturnsForbidden() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateRequirementReferenceRequest("UC-01", "User Login", RequirementSource.USE_CASE, "User can log in"));

        mockMvc.perform(post("/requirements/references").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRequirementAsAdminReturnsData() throws Exception {
        when(requirementTraceabilityService.createRequirement(any())).thenReturn(sampleReference());

        String body = objectMapper.writeValueAsString(
                new CreateRequirementReferenceRequest("UC-01", "User Login", RequirementSource.USE_CASE, "User can log in"));

        mockMvc.perform(post("/requirements/references").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Create requirement reference success"));
    }

    // ─── GET /requirements/trace-links ───────────────────────────────────────────

    @Test
    void getTraceabilityLinksWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/requirements/trace-links"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getTraceabilityLinksAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(get("/requirements/trace-links"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getTraceabilityLinksAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(get("/requirements/trace-links"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTraceabilityLinksAsAdminReturnsData() throws Exception {
        when(requirementTraceabilityService.findTraceabilityLinks()).thenReturn(List.of(sampleLink()));

        mockMvc.perform(get("/requirements/trace-links"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find traceability links success"));
    }

    // ─── POST /requirements/trace-links ──────────────────────────────────────────

    @Test
    void createTraceabilityLinkWithoutAuthenticationReturnsUnauthorized() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateTraceabilityLinkRequest(1L, ComponentType.BACKEND_CONTROLLER, "AuthController", TraceabilityStatus.IMPLEMENTED, "Handles login"));

        mockMvc.perform(post("/requirements/trace-links").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createTraceabilityLinkAsStudentReturnsForbidden() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateTraceabilityLinkRequest(1L, ComponentType.BACKEND_CONTROLLER, "AuthController", TraceabilityStatus.IMPLEMENTED, "Handles login"));

        mockMvc.perform(post("/requirements/trace-links").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createTraceabilityLinkAsInstructorReturnsForbidden() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CreateTraceabilityLinkRequest(1L, ComponentType.BACKEND_CONTROLLER, "AuthController", TraceabilityStatus.IMPLEMENTED, "Handles login"));

        mockMvc.perform(post("/requirements/trace-links").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTraceabilityLinkAsAdminReturnsData() throws Exception {
        when(requirementTraceabilityService.createTraceabilityLink(any())).thenReturn(sampleLink());

        String body = objectMapper.writeValueAsString(
                new CreateTraceabilityLinkRequest(1L, ComponentType.BACKEND_CONTROLLER, "AuthController", TraceabilityStatus.IMPLEMENTED, "Handles login"));

        mockMvc.perform(post("/requirements/trace-links").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Create traceability link success"));
    }
}
