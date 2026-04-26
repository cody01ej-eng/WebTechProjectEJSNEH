package com.tcu.projectpulse.project.controller;

import com.tcu.projectpulse.project.dto.CreateRubricRequest;
import com.tcu.projectpulse.project.dto.RubricResponse;
import com.tcu.projectpulse.project.service.RubricService;
import com.tcu.projectpulse.shared.api.Result;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/rubrics")
public class RubricController {

    private final RubricService rubricService;

    public RubricController(RubricService rubricService) {
        this.rubricService = rubricService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Result<List<RubricResponse>> getRubrics() {
        return Result.success("Find rubrics success", rubricService.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<RubricResponse> createRubric(@Valid @RequestBody CreateRubricRequest request) {
        return Result.success("Create rubric success", rubricService.createRubric(request));
    }
}
