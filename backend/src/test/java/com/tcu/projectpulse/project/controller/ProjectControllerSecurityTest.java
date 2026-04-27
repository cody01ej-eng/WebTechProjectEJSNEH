package com.tcu.projectpulse.project.controller;

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
import com.tcu.projectpulse.project.domain.ActivityCategory;
import com.tcu.projectpulse.project.domain.ActivityProgressStatus;
import com.tcu.projectpulse.project.dto.SectionPeerEvaluationReportResponse;
import com.tcu.projectpulse.project.dto.StudentPeerEvaluationReportResponse;
import com.tcu.projectpulse.project.dto.StudentWarReportResponse;
import com.tcu.projectpulse.project.dto.StudentWorkspaceContextResponse;
import com.tcu.projectpulse.project.dto.TeamWarReportResponse;
import com.tcu.projectpulse.project.dto.WeeklyActivityResponse;
import com.tcu.projectpulse.project.service.ReportingService;
import com.tcu.projectpulse.project.service.StudentWorkspaceService;
import com.tcu.projectpulse.project.service.WarService;
import com.tcu.projectpulse.shared.api.StatusCode;
import java.math.BigDecimal;
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
class ProjectControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WarService warService;

    @MockitoBean
    private ReportingService reportingService;

    @MockitoBean
    private StudentWorkspaceService studentWorkspaceService;

    @MockitoBean(name = "authorizationService")
    private AuthorizationService authorizationService;

    @Test
    void createWarActivityWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/project/war/activities")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validWarRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createWarActivityWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/project/war/activities")
                        .contentType(APPLICATION_JSON)
                        .content(validWarRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("CSRF token missing or invalid"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createWarActivityAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(post("/project/war/activities")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validWarRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createWarActivityAsStudentReturnsSavedActivity() throws Exception {
        when(warService.createActivity(any())).thenReturn(new WeeklyActivityResponse(
                31L,
                7L,
                "Jane Doe",
                11L,
                "Pulse Team",
                LocalDate.of(2026, 4, 20),
                ActivityCategory.DEVELOPMENT,
                "Build the secured dashboard",
                "Implemented the authenticated routing flow.",
                BigDecimal.valueOf(4.0),
                BigDecimal.valueOf(3.5),
                ActivityProgressStatus.DONE
        ));

        mockMvc.perform(post("/project/war/activities")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validWarRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.studentId").value(7))
                .andExpect(jsonPath("$.data.teamName").value("Pulse Team"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getStudentSelfReportReturnsForbiddenWhenAuthorizationServiceRejectsAccess() throws Exception {
        when(authorizationService.canAccessStudentSelfReport(7L)).thenReturn(false);

        mockMvc.perform(get("/project/reports/students/7/peer-evaluations/self")
                        .param("weekStartDate", "2026-04-20"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getStudentSelfReportReturnsDataWhenAuthorizationServiceAllowsAccess() throws Exception {
        when(authorizationService.canAccessStudentSelfReport(7L)).thenReturn(true);
        when(reportingService.getStudentSelfPeerEvaluationReport(7L, LocalDate.of(2026, 4, 20))).thenReturn(
                new StudentPeerEvaluationReportResponse(
                        7L,
                        "Jane Doe",
                        List.of(new StudentPeerEvaluationReportResponse.WeekReport(
                                LocalDate.of(2026, 4, 20),
                                BigDecimal.valueOf(9.5),
                                List.of(),
                                List.of("Helpful teammate")
                        ))
                )
        );

        mockMvc.perform(get("/project/reports/students/7/peer-evaluations/self")
                        .param("weekStartDate", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.studentId").value(7))
                .andExpect(jsonPath("$.data.weeks[0].publicComments[0]").value("Helpful teammate"));
    }

    @Test
    void getStudentWorkspaceWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/project/workspace/student"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getStudentWorkspaceAsStudentReturnsContext() throws Exception {
        when(studentWorkspaceService.getCurrentStudentWorkspaceContext()).thenReturn(new StudentWorkspaceContextResponse(
                7L,
                "Jane Doe",
                "jane.doe@tcu.edu",
                12L,
                "Senior Design A",
                19L,
                "Pulse Team",
                3L,
                "Teamwork Rubric",
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 4, 20),
                List.of(LocalDate.of(2026, 4, 20)),
                List.of(new StudentWorkspaceContextResponse.TeammateView(7L, "Jane Doe", true)),
                List.of(new StudentWorkspaceContextResponse.RubricCriterionView(
                        41L,
                        "Communication",
                        "Shares updates with the team.",
                        BigDecimal.TEN,
                        1
                ))
        ));

        mockMvc.perform(get("/project/workspace/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.studentId").value(7))
                .andExpect(jsonPath("$.data.teamName").value("Pulse Team"))
                .andExpect(jsonPath("$.data.rubricCriteria[0].name").value("Communication"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getStudentWorkspaceAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(get("/project/workspace/student"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getTeamWarReportReturnsForbiddenWhenAuthorizationServiceRejectsAccess() throws Exception {
        when(authorizationService.canAccessTeamWarReport(19L)).thenReturn(false);

        mockMvc.perform(get("/project/reports/teams/19/war")
                        .param("weekStartDate", "2026-04-20"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getTeamWarReportReturnsDataWhenAuthorizationServiceAllowsAccess() throws Exception {
        when(authorizationService.canAccessTeamWarReport(19L)).thenReturn(true);
        when(reportingService.getTeamWarReport(19L, LocalDate.of(2026, 4, 20))).thenReturn(new TeamWarReportResponse(
                19L,
                "Pulse Team",
                LocalDate.of(2026, 4, 20),
                List.of("Alex Roe"),
                List.of(new TeamWarReportResponse.StudentActivityGroup(
                        7L,
                        "Jane Doe",
                        List.of(new WeeklyActivityResponse(
                                31L,
                                7L,
                                "Jane Doe",
                                19L,
                                "Pulse Team",
                                LocalDate.of(2026, 4, 20),
                                ActivityCategory.DEVELOPMENT,
                                "Build the secured dashboard",
                                "Implemented the authenticated routing flow.",
                                BigDecimal.valueOf(4.0),
                                BigDecimal.valueOf(3.5),
                                ActivityProgressStatus.DONE
                        ))
                ))
        ));

        mockMvc.perform(get("/project/reports/teams/19/war")
                        .param("weekStartDate", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.teamName").value("Pulse Team"))
                .andExpect(jsonPath("$.data.students[0].activities[0].studentId").value(7));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getStudentWarReportAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(get("/project/reports/students/7/war")
                        .param("fromWeekStartDate", "2026-04-13")
                        .param("toWeekStartDate", "2026-04-20"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getStudentWarReportAsInstructorReturnsData() throws Exception {
        when(reportingService.getStudentWarReport(7L, LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 20))).thenReturn(
                new StudentWarReportResponse(
                        7L,
                        "Jane Doe",
                        List.of(new StudentWarReportResponse.WeekActivities(
                                LocalDate.of(2026, 4, 20),
                                List.of(new WeeklyActivityResponse(
                                        31L,
                                        7L,
                                        "Jane Doe",
                                        19L,
                                        "Pulse Team",
                                        LocalDate.of(2026, 4, 20),
                                        ActivityCategory.DEVELOPMENT,
                                        "Build the secured dashboard",
                                        "Implemented the authenticated routing flow.",
                                        BigDecimal.valueOf(4.0),
                                        BigDecimal.valueOf(3.5),
                                        ActivityProgressStatus.DONE
                                ))
                        ))
                )
        );

        mockMvc.perform(get("/project/reports/students/7/war")
                        .param("fromWeekStartDate", "2026-04-13")
                        .param("toWeekStartDate", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.studentId").value(7))
                .andExpect(jsonPath("$.data.weeks[0].activities[0].teamName").value("Pulse Team"));
    }

    // ─── PUT /project/war/activities/{activityId} ──────────────────────────────────

    @Test
    void updateWarActivityWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(put("/project/war/activities/31")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validWarRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void updateWarActivityWithoutCsrfTokenReturnsForbidden() throws Exception {
        mockMvc.perform(put("/project/war/activities/31")
                        .contentType(APPLICATION_JSON)
                        .content(validWarRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("CSRF token missing or invalid"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void updateWarActivityAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(put("/project/war/activities/31")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validWarRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void updateWarActivityAsStudentReturnsData() throws Exception {
        when(warService.updateActivity(eq(31L), any())).thenReturn(
                new WeeklyActivityResponse(
                        31L, 7L, "Jane Doe", 19L, "Pulse Team",
                        LocalDate.of(2026, 4, 20),
                        ActivityCategory.DEVELOPMENT,
                        "Build the secured dashboard",
                        "Implemented the authenticated routing flow.",
                        BigDecimal.valueOf(4.0), BigDecimal.valueOf(3.5),
                        ActivityProgressStatus.DONE
                )
        );

        mockMvc.perform(put("/project/war/activities/31")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(validWarRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.id").value(31));
    }

    // ─── DELETE /project/war/activities/{activityId} ─────────────────────────────

    @Test
    void deleteWarActivityWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/project/war/activities/31").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void deleteWarActivityAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/project/war/activities/31").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void deleteWarActivityAsStudentReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/project/war/activities/31").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Delete WAR activity success"));
    }

    // ─── GET /project/reports/sections/{sectionId}/peer-evaluations ───────────────

    @Test
    void getSectionPeerEvaluationReportWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/project/reports/sections/12/peer-evaluations")
                        .param("weekStartDate", "2026-04-20"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getSectionPeerEvaluationReportAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(get("/project/reports/sections/12/peer-evaluations")
                        .param("weekStartDate", "2026-04-20"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getSectionPeerEvaluationReportAsInstructorReturnsData() throws Exception {
        when(reportingService.getSectionPeerEvaluationReport(12L, LocalDate.of(2026, 4, 20))).thenReturn(
                new SectionPeerEvaluationReportResponse(12L, "CS-301", LocalDate.of(2026, 4, 20),
                        List.of(), List.of())
        );

        mockMvc.perform(get("/project/reports/sections/12/peer-evaluations")
                        .param("weekStartDate", "2026-04-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.sectionId").value(12));
    }

    // ─── GET /project/reports/students/{studentId}/peer-evaluations/instructor ────

    @Test
    void getInstructorStudentReportWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/project/reports/students/7/peer-evaluations/instructor"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getInstructorStudentReportAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(get("/project/reports/students/7/peer-evaluations/instructor"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getInstructorStudentReportAsInstructorReturnsData() throws Exception {
        when(reportingService.getInstructorStudentPeerEvaluationReport(7L, null, null)).thenReturn(
                new StudentPeerEvaluationReportResponse(7L, "Jane Doe", List.of())
        );

        mockMvc.perform(get("/project/reports/students/7/peer-evaluations/instructor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.studentId").value(7));
    }

    private String validWarRequest() throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "studentId", 7,
                "weekStartDate", "2026-04-20",
                "category", "DEVELOPMENT",
                "plannedActivity", "Build the secured dashboard",
                "description", "Implemented the authenticated routing flow.",
                "plannedHours", 4.0,
                "actualHours", 3.5,
                "status", "DONE"
        ));
    }
}
