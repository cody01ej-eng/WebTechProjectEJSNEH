package com.tcu.projectpulse.project.controller;

import com.tcu.projectpulse.project.dto.StudentWorkspaceContextResponse;
import com.tcu.projectpulse.project.service.StudentWorkspaceService;
import com.tcu.projectpulse.shared.api.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/workspace")
public class StudentWorkspaceController {

    private final StudentWorkspaceService studentWorkspaceService;

    public StudentWorkspaceController(StudentWorkspaceService studentWorkspaceService) {
        this.studentWorkspaceService = studentWorkspaceService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student")
    public Result<StudentWorkspaceContextResponse> getStudentWorkspaceContext() {
        return Result.success("Student workspace context loaded", studentWorkspaceService.getCurrentStudentWorkspaceContext());
    }
}
