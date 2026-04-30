package com.tcu.projectpulse.user.dto;

public record StudentDeletionResponse(Long studentId,
                                      String studentName,
                                      int deletedWarActivities,
                                      int deletedPeerEvaluationSubmissions,
                                      int deletedPeerEvaluationItems) {
}
