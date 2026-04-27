package com.tcu.projectpulse.project.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcu.projectpulse.project.dto.StudentWorkspaceContextResponse;
import com.tcu.projectpulse.project.service.StudentWorkspaceService;
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
class StudentWorkspaceControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentWorkspaceService studentWorkspaceService;

    private StudentWorkspaceContextResponse sampleContext() {
        return new StudentWorkspaceContextResponse(
                1L,
                "Jane Doe",
                "jane.doe@tcu.edu",
                10L,
                "2026-2027",
                20L,
                "Team Alpha",
                null,
                null,
                LocalDate.of(2026, 4, 27),
                null,
                List.of(LocalDate.of(2026, 4, 27)),
                List.of(),
                List.of()
        );
    }

    // ─── GET /project/workspace/student ──────────────────────────────────────────

    @Test
    void getStudentWorkspaceWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/project/workspace/student"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStudentWorkspaceAsAdminReturnsForbidden() throws Exception {
        mockMvc.perform(get("/project/workspace/student"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getStudentWorkspaceAsInstructorReturnsForbidden() throws Exception {
        mockMvc.perform(get("/project/workspace/student"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getStudentWorkspaceAsStudentReturnsData() throws Exception {
        when(studentWorkspaceService.getCurrentStudentWorkspaceContext()).thenReturn(sampleContext());

        mockMvc.perform(get("/project/workspace/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Student workspace context loaded"))
                .andExpect(jsonPath("$.data.studentName").value("Jane Doe"))
                .andExpect(jsonPath("$.data.teamName").value("Team Alpha"));
    }
}
