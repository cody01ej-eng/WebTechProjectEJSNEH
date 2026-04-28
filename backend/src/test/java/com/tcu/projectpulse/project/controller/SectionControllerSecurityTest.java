package com.tcu.projectpulse.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcu.projectpulse.project.dto.SectionResponse;
import com.tcu.projectpulse.project.service.SectionService;
import com.tcu.projectpulse.shared.api.StatusCode;
import com.tcu.projectpulse.shared.exception.ConflictException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import java.time.LocalDate;
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
class SectionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SectionService sectionService;

    // --- GET /project/sections (UC-2) ---

    @Test
    void getSectionsWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/project/sections"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getSectionsAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(get("/project/sections"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSectionsAsAdminReturnsSectionList() throws Exception {
        when(sectionService.findSections(null)).thenReturn(List.of(sampleSection()));

        mockMvc.perform(get("/project/sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find sections success"))
                .andExpect(jsonPath("$.data[0].name").value("Senior Design A"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSectionsWithNameFilterPassesParamToService() throws Exception {
        when(sectionService.findSections("design")).thenReturn(List.of(sampleSection()));

        mockMvc.perform(get("/project/sections").param("name", "design"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10));
    }

    // --- GET /project/sections/{sectionId} (UC-3) ---

    @Test
    void getSectionWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/project/sections/10"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSectionAsAdminReturnsSectionDetails() throws Exception {
        when(sectionService.getSection(10L)).thenReturn(sampleSection());

        mockMvc.perform(get("/project/sections/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find section success"))
                .andExpect(jsonPath("$.data.name").value("Senior Design A"))
                .andExpect(jsonPath("$.data.rubricName").value("Peer Eval Rubric v1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSectionAsAdminWithUnknownIdReturnsNotFound() throws Exception {
        when(sectionService.getSection(99L))
                .thenThrow(new ResourceNotFoundException("Section not found with id 99"));

        mockMvc.perform(get("/project/sections/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Section not found with id 99"));
    }

    // --- POST /project/sections (UC-4) ---

    @Test
    void createSectionWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/project/sections")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validCreateSectionRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createSectionAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(post("/project/sections")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validCreateSectionRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSectionAsAdminReturnsCreatedSection() throws Exception {
        when(sectionService.createSection(any())).thenReturn(sampleSection());

        mockMvc.perform(post("/project/sections")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validCreateSectionRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Create section success"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.name").value("Senior Design A"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSectionAsAdminWithBlankNameReturnsBadRequest() throws Exception {
        String invalidBody = objectMapper.writeValueAsString(Map.of(
                "name", "",
                "startDate", "2026-08-24",
                "endDate", "2027-05-01",
                "rubricId", 2
        ));

        mockMvc.perform(post("/project/sections")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSectionAsAdminWithDuplicateNameReturnsConflict() throws Exception {
        when(sectionService.createSection(any()))
                .thenThrow(new ConflictException("Section name already exists: Senior Design A"));

        mockMvc.perform(post("/project/sections")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validCreateSectionRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.CONFLICT))
                .andExpect(jsonPath("$.message").value("Section name already exists: Senior Design A"));
    }

    // --- PUT /project/sections/{sectionId} (UC-5) ---

    @Test
    void updateSectionWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(put("/project/sections/10")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validUpdateSectionRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSectionAsAdminReturnsUpdatedSection() throws Exception {
        SectionResponse updated = new SectionResponse(
                10L, "Renamed Section",
                LocalDate.of(2026, 8, 24), LocalDate.of(2027, 5, 1),
                2L, "Peer Eval Rubric v1", List.of(), List.of()
        );
        when(sectionService.updateSection(eq(10L), any())).thenReturn(updated);

        mockMvc.perform(put("/project/sections/10")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validUpdateSectionRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update section success"))
                .andExpect(jsonPath("$.data.name").value("Renamed Section"));
    }

    // --- POST /project/sections/{sectionId}/active-weeks (UC-6) ---

    @Test
    void configureActiveWeeksWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/project/sections/10/active-weeks")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{\"inactiveWeekStartDates\":[]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void configureActiveWeeksAsAdminReturnsUpdatedSection() throws Exception {
        when(sectionService.configureActiveWeeks(eq(10L), any())).thenReturn(sampleSection());

        mockMvc.perform(post("/project/sections/10/active-weeks")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{\"inactiveWeekStartDates\":[\"2026-12-21\",\"2026-12-28\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Configure active weeks success"))
                .andExpect(jsonPath("$.data.name").value("Senior Design A"));
    }

    // --- helpers ---

    private SectionResponse sampleSection() {
        return new SectionResponse(
                10L,
                "Senior Design A",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                2L,
                "Peer Eval Rubric v1",
                List.of(new SectionResponse.WeekView(1L, LocalDate.of(2026, 8, 24), true)),
                List.of()
        );
    }

    private String validCreateSectionRequest() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "name", "Senior Design A",
                "startDate", "2026-08-24",
                "endDate", "2027-05-01",
                "rubricId", 2
        ));
    }

    private String validUpdateSectionRequest() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "name", "Renamed Section",
                "startDate", "2026-08-24",
                "endDate", "2027-05-01",
                "rubricId", 2
        ));
    }
}
