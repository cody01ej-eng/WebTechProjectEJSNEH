package com.tcu.projectpulse.project.controller;

import com.tcu.projectpulse.project.dto.PeerEvaluationSubmissionResponse;
import com.tcu.projectpulse.project.dto.SubmitPeerEvaluationRequest;
import com.tcu.projectpulse.project.service.PeerEvaluationService;
import com.tcu.projectpulse.shared.api.Result;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/peer-evaluations")
public class PeerEvaluationController {

    private final PeerEvaluationService peerEvaluationService;

    public PeerEvaluationController(PeerEvaluationService peerEvaluationService) {
        this.peerEvaluationService = peerEvaluationService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public Result<PeerEvaluationSubmissionResponse> submitEvaluation(@Valid @RequestBody SubmitPeerEvaluationRequest request) {
        return Result.success("Submit peer evaluation success", peerEvaluationService.submitEvaluation(request));
    }
}
