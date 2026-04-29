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
import com.tcu.projectpulse.project.dto.AssignInstructorsRequest;
import com.tcu.projectpulse.project.dto.AssignStudentsRequest;
import com.tcu.projectpulse.project.dto.CreateTeamRequest;
import com.tcu.projectpulse.project.dto.TeamResponse;
import com.tcu.projectpulse.project.dto.UpdateTeamRequest;
import com.tcu.projectpulse.project.service.TeamService;
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
class TeamControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeamService teamService;

    private TeamResponse sampleTeam() {
        return new TeamResponse(1L, "2026-2027", "Team Alpha", "Senior design team", null, List.of(), List.of());
    }

    // ─── GET /project/teams ──────────────────────────────────────────────────────

    @Test
    void getTeamsWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/project/teams"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getTeamsAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(get("/project/teams"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeamsAsAdminReturnsData() throws Exception {
        when(teamService.findTeams(null)).thenReturn(List.of(sampleTeam()));

        mockMvc.perform(get("/project/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find teams success"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getTeamsAsInstructorReturnsData() throws Exception {
        when(teamService.findTeams(null)).thenReturn(List.of(sampleTeam()));

        mockMvc.perform(get("/project/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find teams success"));
    }

    // ─── GET /project/teams/{teamId} ─────────────────────────────────────────────

    @Test
    void getTeamWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/project/teams/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getTeamAsStudentReturnsForbidden() throws Exception {
        mockMvc.perform(get("/project/teams/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeamAsAdminReturnsData() throws Exception {
        when(teamService.getTeam(1L)).thenReturn(sampleTeam());

        mockMvc.perform(get("/project/teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find team success"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getTeamAsInstructorReturnsData() throws Exception {
        when(teamService.getTeam(1L)).thenReturn(sampleTeam());

        mockMvc.perform(get("/project/teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find team success"));
    }

    // ─── POST /project/teams ─────────────────────────────────────────────────────

    @Test
    void createTeamWithoutAuthenticationReturnsUnauthorized() throws Exception {
        CreateTeamRequest request = new CreateTeamRequest(1L, "Team Alpha", "Senior design team", null);

        mockMvc.perform(post("/project/teams")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createTeamAsInstructorReturnsForbidden() throws Exception {
        CreateTeamRequest request = new CreateTeamRequest(1L, "Team Alpha", "Senior design team", null);

        mockMvc.perform(post("/project/teams")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeamAsAdminReturnsData() throws Exception {
        CreateTeamRequest request = new CreateTeamRequest(1L, "Team Alpha", "Senior design team", null);
        when(teamService.createTeam(any())).thenReturn(sampleTeam());

        mockMvc.perform(post("/project/teams")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Create team success"));
    }

    // ─── PUT /project/teams/{teamId} ─────────────────────────────────────────────

    @Test
    void updateTeamWithoutAuthenticationReturnsUnauthorized() throws Exception {
        UpdateTeamRequest request = new UpdateTeamRequest("Team Alpha Updated", "Updated description", null);

        mockMvc.perform(put("/project/teams/1")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void updateTeamAsInstructorReturnsForbidden() throws Exception {
        UpdateTeamRequest request = new UpdateTeamRequest("Team Alpha Updated", "Updated description", null);

        mockMvc.perform(put("/project/teams/1")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTeamAsAdminReturnsData() throws Exception {
        UpdateTeamRequest request = new UpdateTeamRequest("Team Alpha Updated", "Updated description", null);
        when(teamService.updateTeam(eq(1L), any())).thenReturn(sampleTeam());

        mockMvc.perform(put("/project/teams/1")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Update team success"));
    }

    // ─── POST /project/teams/{teamId}/students ───────────────────────────────────

    @Test
    void assignStudentsWithoutAuthenticationReturnsUnauthorized() throws Exception {
        AssignStudentsRequest request = new AssignStudentsRequest(List.of(2L));

        mockMvc.perform(post("/project/teams/1/students")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void assignStudentsAsInstructorReturnsForbidden() throws Exception {
        AssignStudentsRequest request = new AssignStudentsRequest(List.of(2L));

        mockMvc.perform(post("/project/teams/1/students")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignStudentsAsAdminReturnsData() throws Exception {
        AssignStudentsRequest request = new AssignStudentsRequest(List.of(2L));
        when(teamService.assignStudents(eq(1L), any())).thenReturn(sampleTeam());

        mockMvc.perform(post("/project/teams/1/students")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Assign students success"));
    }

    // ─── POST /project/teams/{teamId}/instructors ────────────────────────────────

    @Test
    void assignInstructorsWithoutAuthenticationReturnsUnauthorized() throws Exception {
        AssignInstructorsRequest request = new AssignInstructorsRequest(List.of(3L));

        mockMvc.perform(post("/project/teams/1/instructors")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void assignInstructorsAsInstructorReturnsForbidden() throws Exception {
        AssignInstructorsRequest request = new AssignInstructorsRequest(List.of(3L));

        mockMvc.perform(post("/project/teams/1/instructors")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignInstructorsAsAdminReturnsData() throws Exception {
        AssignInstructorsRequest request = new AssignInstructorsRequest(List.of(3L));
        when(teamService.assignInstructors(eq(1L), any())).thenReturn(sampleTeam());

        mockMvc.perform(post("/project/teams/1/instructors")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Assign instructors success"));
    }

    // ─── DELETE /project/teams/{teamId}/students/{studentId} ─────────────────────

    @Test
    void removeStudentWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/project/teams/1/students/2")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void removeStudentAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/project/teams/1/students/2")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeStudentAsAdminReturnsData() throws Exception {
        when(teamService.removeStudent(1L, 2L)).thenReturn(sampleTeam());

        mockMvc.perform(delete("/project/teams/1/students/2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Remove student success"));
    }

    // ─── DELETE /project/teams/{teamId}/instructors/{instructorId} ───────────────

    @Test
    void removeInstructorWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/project/teams/1/instructors/3")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void removeInstructorAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/project/teams/1/instructors/3")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeInstructorAsAdminReturnsData() throws Exception {
        when(teamService.removeInstructor(1L, 3L)).thenReturn(sampleTeam());

        mockMvc.perform(delete("/project/teams/1/instructors/3")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Remove instructor success"));
    }
}
