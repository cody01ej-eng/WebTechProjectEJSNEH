package com.tcu.projectpulse.project.controller;

import com.tcu.projectpulse.project.dto.WarActivityRequest;
import com.tcu.projectpulse.project.dto.WeeklyActivityResponse;
import com.tcu.projectpulse.project.service.WarService;
import com.tcu.projectpulse.shared.api.Result;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/war/activities")
public class WarController {

    private final WarService warService;

    public WarController(WarService warService) {
        this.warService = warService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public Result<WeeklyActivityResponse> createActivity(@Valid @RequestBody WarActivityRequest request) {
        return Result.success("Create WAR activity success", warService.createActivity(request));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping("/{activityId}")
    public Result<WeeklyActivityResponse> updateActivity(@PathVariable Long activityId,
                                                         @Valid @RequestBody WarActivityRequest request) {
        return Result.success("Update WAR activity success", warService.updateActivity(activityId, request));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @DeleteMapping("/{activityId}")
    public Result<Void> deleteActivity(@PathVariable Long activityId) {
        warService.deleteActivity(activityId);
        return Result.success("Delete WAR activity success");
    }
}
