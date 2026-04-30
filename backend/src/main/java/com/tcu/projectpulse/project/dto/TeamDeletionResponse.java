package com.tcu.projectpulse.project.dto;

public record TeamDeletionResponse(Long teamId,
                                   String teamName,
                                   int removedStudentAssignments,
                                   int removedInstructorAssignments,
                                   int deletedWarActivities,
                                   int deletedPeerEvaluationSubmissions) {
}
