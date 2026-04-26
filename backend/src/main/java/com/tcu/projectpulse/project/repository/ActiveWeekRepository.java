package com.tcu.projectpulse.project.repository;

import com.tcu.projectpulse.project.domain.ActiveWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActiveWeekRepository extends JpaRepository<ActiveWeek, Long> {

    List<ActiveWeek> findBySectionIdOrderByWeekStartDateAsc(Long sectionId);

    Optional<ActiveWeek> findBySectionIdAndWeekStartDate(Long sectionId, LocalDate weekStartDate);
}
