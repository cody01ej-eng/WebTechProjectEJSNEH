package com.tcu.projectpulse.project.repository;

import com.tcu.projectpulse.project.domain.SeniorDesignTeam;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeniorDesignTeamRepository extends JpaRepository<SeniorDesignTeam, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<SeniorDesignTeam> findByNameIgnoreCase(String name);

    List<SeniorDesignTeam> findBySectionIdOrderByNameAsc(Long sectionId);
}
