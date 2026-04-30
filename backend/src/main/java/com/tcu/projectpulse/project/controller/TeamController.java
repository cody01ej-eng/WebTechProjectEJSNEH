package com.tcu.projectpulse.project.controller;

import com.tcu.projectpulse.project.dto.AssignInstructorsRequest;
import com.tcu.projectpulse.project.dto.AssignStudentsRequest;
import com.tcu.projectpulse.project.dto.CreateTeamRequest;
import com.tcu.projectpulse.project.dto.TeamDeletionResponse;
import com.tcu.projectpulse.project.dto.TeamResponse;
import com.tcu.projectpulse.project.dto.UpdateTeamRequest;
import com.tcu.projectpulse.project.service.TeamService;
import com.tcu.projectpulse.shared.api.Result;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping
    public Result<List<TeamResponse>> getTeams(@RequestParam(required = false) Long sectionId) {
        return Result.success("Find teams success", teamService.findTeams(sectionId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping("/{teamId}")
    public Result<TeamResponse> getTeam(@PathVariable Long teamId) {
        return Result.success("Find team success", teamService.getTeam(teamId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        return Result.success("Create team success", teamService.createTeam(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{teamId}")
    public Result<TeamResponse> updateTeam(@PathVariable Long teamId,
                                           @Valid @RequestBody UpdateTeamRequest request) {
        return Result.success("Update team success", teamService.updateTeam(teamId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{teamId}/students")
    public Result<TeamResponse> assignStudents(@PathVariable Long teamId,
                                               @Valid @RequestBody AssignStudentsRequest request) {
        return Result.success("Assign students success", teamService.assignStudents(teamId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{teamId}/instructors")
    public Result<TeamResponse> assignInstructors(@PathVariable Long teamId,
                                                  @Valid @RequestBody AssignInstructorsRequest request) {
        return Result.success("Assign instructors success", teamService.assignInstructors(teamId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{teamId}/students/{studentId}")
    public Result<TeamResponse> removeStudent(@PathVariable Long teamId,
                                              @PathVariable Long studentId) {
        return Result.success("Remove student success", teamService.removeStudent(teamId, studentId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{teamId}/instructors/{instructorId}")
    public Result<TeamResponse> removeInstructor(@PathVariable Long teamId,
                                                 @PathVariable Long instructorId) {
        return Result.success("Remove instructor success", teamService.removeInstructor(teamId, instructorId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{teamId}")
    public Result<TeamDeletionResponse> deleteTeam(@PathVariable Long teamId) {
        return Result.success("Delete team success", teamService.deleteTeam(teamId));
    }
}
