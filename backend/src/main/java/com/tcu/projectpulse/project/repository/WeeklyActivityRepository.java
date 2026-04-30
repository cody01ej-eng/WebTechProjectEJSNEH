package com.tcu.projectpulse.project.repository;

import com.tcu.projectpulse.project.domain.WeeklyActivity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyActivityRepository extends JpaRepository<WeeklyActivity, Long> {

    boolean existsByTeamSectionId(Long sectionId);

    List<WeeklyActivity> findByTeamId(Long teamId);

    List<WeeklyActivity> findByStudentId(Long studentId);

    List<WeeklyActivity> findByTeamIdAndActiveWeekWeekStartDateOrderByStudentLastNameAscStudentFirstNameAsc(Long teamId,
                                                                                                            java.time.LocalDate weekStartDate);

    List<WeeklyActivity> findByStudentIdAndActiveWeekWeekStartDateBetweenOrderByActiveWeekWeekStartDateAsc(Long studentId,
                                                                                                           java.time.LocalDate from,
                                                                                                           java.time.LocalDate to);

    List<WeeklyActivity> findByStudentIdAndActiveWeekId(Long studentId, Long activeWeekId);
}
