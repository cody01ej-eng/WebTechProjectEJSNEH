package com.tcu.projectpulse.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcu.projectpulse.project.dto.RubricResponse;
import com.tcu.projectpulse.project.service.RubricService;
import com.tcu.projectpulse.shared.api.StatusCode;
import com.tcu.projectpulse.shared.exception.ConflictException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RubricControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RubricService rubricService;

    // --- POST /project/rubrics ---

    @Test
    void createRubricWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/project/rubrics")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validCreateRubricRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRubricWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/project/rubrics")
                        .contentType(APPLICATION_JSON)
                        .content(validCreateRubricRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("CSRF token missing or invalid"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createRubricAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(post("/project/rubrics")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validCreateRubricRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createRubricAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(post("/project/rubrics")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validCreateRubricRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRubricAsAdminReturnsCreatedRubric() throws Exception {
        when(rubricService.createRubric(any())).thenReturn(new RubricResponse(
                1L,
                "Peer Eval Rubric v1",
                List.of(new RubricResponse.Criterion(1L, "Quality of work", "Does high quality work", BigDecimal.valueOf(10), 1))
        ));

        mockMvc.perform(post("/project/rubrics")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validCreateRubricRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Create rubric success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Peer Eval Rubric v1"))
                .andExpect(jsonPath("$.data.criteria[0].name").value("Quality of work"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRubricAsAdminWithBlankNameReturnsBadRequest() throws Exception {
        String invalidBody = objectMapper.writeValueAsString(Map.of(
                "name", "",
                "criteria", List.of(Map.of(
                        "name", "Quality",
                        "description", "Quality criterion",
                        "maxScore", 10
                ))
        ));

        mockMvc.perform(post("/project/rubrics")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRubricAsAdminWithDuplicateNameReturnsConflict() throws Exception {
        when(rubricService.createRubric(any()))
                .thenThrow(new ConflictException("Rubric name already exists: Peer Eval Rubric v1"));

        mockMvc.perform(post("/project/rubrics")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validCreateRubricRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.CONFLICT))
                .andExpect(jsonPath("$.message").value("Rubric name already exists: Peer Eval Rubric v1"));
    }

    // --- GET /project/rubrics ---

    @Test
    void getRubricsWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/project/rubrics"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getRubricsAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(get("/project/rubrics"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRubricsAsAdminReturnsRubricList() throws Exception {
        when(rubricService.findAll()).thenReturn(List.of(
                new RubricResponse(1L, "Alpha Rubric", List.of()),
                new RubricResponse(2L, "Beta Rubric", List.of())
        ));

        mockMvc.perform(get("/project/rubrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find rubrics success"))
                .andExpect(jsonPath("$.data[0].name").value("Alpha Rubric"))
                .andExpect(jsonPath("$.data[1].name").value("Beta Rubric"));
    }

    private String validCreateRubricRequest() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "name", "Peer Eval Rubric v1",
                "criteria", List.of(Map.of(
                        "name", "Quality of work",
                        "description", "Does high quality work",
                        "maxScore", 10
                ))
        ));
    }
}
