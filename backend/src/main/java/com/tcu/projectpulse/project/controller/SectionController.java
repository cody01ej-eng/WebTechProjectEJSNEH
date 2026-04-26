package com.tcu.projectpulse.project.controller;

import com.tcu.projectpulse.project.dto.ActiveWeekSetupRequest;
import com.tcu.projectpulse.project.dto.CreateSectionRequest;
import com.tcu.projectpulse.project.dto.SectionResponse;
import com.tcu.projectpulse.project.dto.UpdateSectionRequest;
import com.tcu.projectpulse.project.service.SectionService;
import com.tcu.projectpulse.shared.api.Result;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Result<List<SectionResponse>> getSections(@RequestParam(required = false) String name) {
        return Result.success("Find sections success", sectionService.findSections(name));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{sectionId}")
    public Result<SectionResponse> getSection(@PathVariable Long sectionId) {
        return Result.success("Find section success", sectionService.getSection(sectionId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<SectionResponse> createSection(@Valid @RequestBody CreateSectionRequest request) {
        return Result.success("Create section success", sectionService.createSection(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{sectionId}")
    public Result<SectionResponse> updateSection(@PathVariable Long sectionId,
                                                 @Valid @RequestBody UpdateSectionRequest request) {
        return Result.success("Update section success", sectionService.updateSection(sectionId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{sectionId}/active-weeks")
    public Result<SectionResponse> configureActiveWeeks(@PathVariable Long sectionId,
                                                        @Valid @RequestBody ActiveWeekSetupRequest request) {
        return Result.success("Configure active weeks success", sectionService.configureActiveWeeks(sectionId, request));
    }
}
