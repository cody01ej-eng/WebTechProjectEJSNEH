package com.tcu.projectpulse.requirement.controller;

import com.tcu.projectpulse.requirement.dto.CreateRequirementReferenceRequest;
import com.tcu.projectpulse.requirement.dto.CreateTraceabilityLinkRequest;
import com.tcu.projectpulse.requirement.dto.RequirementReferenceResponse;
import com.tcu.projectpulse.requirement.dto.TraceabilityLinkResponse;
import com.tcu.projectpulse.requirement.service.RequirementTraceabilityService;
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
@RequestMapping("/requirements")
public class RequirementTraceabilityController {

    private final RequirementTraceabilityService requirementTraceabilityService;

    public RequirementTraceabilityController(RequirementTraceabilityService requirementTraceabilityService) {
        this.requirementTraceabilityService = requirementTraceabilityService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/references")
    public Result<List<RequirementReferenceResponse>> getRequirements() {
        return Result.success("Find requirements success", requirementTraceabilityService.findRequirements());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/references")
    public Result<RequirementReferenceResponse> createRequirement(@Valid @RequestBody CreateRequirementReferenceRequest request) {
        return Result.success("Create requirement reference success", requirementTraceabilityService.createRequirement(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/trace-links")
    public Result<List<TraceabilityLinkResponse>> getTraceabilityLinks() {
        return Result.success("Find traceability links success", requirementTraceabilityService.findTraceabilityLinks());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/trace-links")
    public Result<TraceabilityLinkResponse> createTraceabilityLink(@Valid @RequestBody CreateTraceabilityLinkRequest request) {
        return Result.success("Create traceability link success", requirementTraceabilityService.createTraceabilityLink(request));
    }
}
