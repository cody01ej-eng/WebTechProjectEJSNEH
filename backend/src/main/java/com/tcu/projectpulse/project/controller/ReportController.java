package com.tcu.projectpulse.project.controller;

import com.tcu.projectpulse.project.dto.SectionPeerEvaluationReportResponse;
import com.tcu.projectpulse.project.dto.StudentPeerEvaluationReportResponse;
import com.tcu.projectpulse.project.dto.StudentWarReportResponse;
import com.tcu.projectpulse.project.dto.TeamWarReportResponse;
import com.tcu.projectpulse.project.service.ReportingService;
import com.tcu.projectpulse.shared.api.Result;
import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/reports")
public class ReportController {

    private final ReportingService reportingService;

    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @PreAuthorize("@authorizationService.canAccessTeamWarReport(#teamId)")
    @GetMapping("/teams/{teamId}/war")
    public Result<TeamWarReportResponse> getTeamWarReport(@PathVariable Long teamId,
                                                          @RequestParam LocalDate weekStartDate) {
        return Result.success("Generate team WAR report success", reportingService.getTeamWarReport(teamId, weekStartDate));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/students/{studentId}/war")
    public Result<StudentWarReportResponse> getStudentWarReport(@PathVariable Long studentId,
                                                                @RequestParam(required = false) LocalDate fromWeekStartDate,
                                                                @RequestParam(required = false) LocalDate toWeekStartDate) {
        return Result.success("Generate student WAR report success",
                reportingService.getStudentWarReport(studentId, fromWeekStartDate, toWeekStartDate));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/sections/{sectionId}/peer-evaluations")
    public Result<SectionPeerEvaluationReportResponse> getSectionPeerEvaluationReport(@PathVariable Long sectionId,
                                                                                      @RequestParam LocalDate weekStartDate) {
        return Result.success("Generate section peer evaluation report success",
                reportingService.getSectionPeerEvaluationReport(sectionId, weekStartDate));
    }

    @PreAuthorize("@authorizationService.canAccessStudentSelfReport(#studentId)")
    @GetMapping("/students/{studentId}/peer-evaluations/self")
    public Result<StudentPeerEvaluationReportResponse> getStudentSelfReport(@PathVariable Long studentId,
                                                                            @RequestParam LocalDate weekStartDate) {
        return Result.success("Generate student self peer evaluation report success",
                reportingService.getStudentSelfPeerEvaluationReport(studentId, weekStartDate));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/students/{studentId}/peer-evaluations/instructor")
    public Result<StudentPeerEvaluationReportResponse> getInstructorStudentReport(@PathVariable Long studentId,
                                                                                  @RequestParam(required = false) LocalDate fromWeekStartDate,
                                                                                  @RequestParam(required = false) LocalDate toWeekStartDate) {
        return Result.success("Generate instructor student peer evaluation report success",
                reportingService.getInstructorStudentPeerEvaluationReport(studentId, fromWeekStartDate, toWeekStartDate));
    }
}
