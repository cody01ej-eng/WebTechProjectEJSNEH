package com.tcu.projectpulse.project.repository;

import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeniorDesignSectionRepository extends JpaRepository<SeniorDesignSection, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<SeniorDesignSection> findByNameIgnoreCase(String name);
}
