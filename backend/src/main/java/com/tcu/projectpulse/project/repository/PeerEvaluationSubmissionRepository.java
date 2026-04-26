package com.tcu.projectpulse.project.repository;

import com.tcu.projectpulse.project.domain.PeerEvaluationSubmission;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeerEvaluationSubmissionRepository extends JpaRepository<PeerEvaluationSubmission, Long> {

    boolean existsByTeamSectionId(Long sectionId);

    boolean existsByAuthorIdAndActiveWeekWeekStartDate(Long authorId, LocalDate weekStartDate);

    Optional<PeerEvaluationSubmission> findByAuthorIdAndActiveWeekWeekStartDate(Long authorId, LocalDate weekStartDate);

    List<PeerEvaluationSubmission> findByTeamSectionIdAndActiveWeekWeekStartDate(Long sectionId, LocalDate weekStartDate);

    List<PeerEvaluationSubmission> findByAuthorAssignedTeamIdAndActiveWeekWeekStartDate(Long teamId, LocalDate weekStartDate);
}
