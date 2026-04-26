package com.tcu.projectpulse.project.repository;

import com.tcu.projectpulse.project.domain.PeerEvaluationItem;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeerEvaluationItemRepository extends JpaRepository<PeerEvaluationItem, Long> {

    List<PeerEvaluationItem> findByEvaluateeIdAndSubmissionActiveWeekWeekStartDate(Long evaluateeId, LocalDate weekStartDate);

    List<PeerEvaluationItem> findByEvaluateeIdAndSubmissionActiveWeekWeekStartDateBetween(Long evaluateeId,
                                                                                           LocalDate from,
                                                                                           LocalDate to);
}
