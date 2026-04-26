package com.tcu.projectpulse.requirement.repository;

import com.tcu.projectpulse.requirement.domain.RequirementReference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementReferenceRepository extends JpaRepository<RequirementReference, Long> {

    boolean existsByReferenceKeyIgnoreCase(String referenceKey);

    Optional<RequirementReference> findByReferenceKeyIgnoreCase(String referenceKey);
}
