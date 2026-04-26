package com.tcu.projectpulse.project.repository;

import com.tcu.projectpulse.project.domain.Rubric;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RubricRepository extends JpaRepository<Rubric, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Rubric> findByNameIgnoreCase(String name);
}
