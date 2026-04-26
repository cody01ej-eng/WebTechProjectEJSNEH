package com.tcu.projectpulse.project.repository;

import com.tcu.projectpulse.project.domain.TeamInstructorAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamInstructorAssignmentRepository extends JpaRepository<TeamInstructorAssignment, Long> {

    List<TeamInstructorAssignment> findByTeamId(Long teamId);

    List<TeamInstructorAssignment> findByInstructorId(Long instructorId);

    boolean existsByTeamIdAndInstructorId(Long teamId, Long instructorId);

    void deleteByTeamIdAndInstructorId(Long teamId, Long instructorId);
}
