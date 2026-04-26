package com.tcu.projectpulse.requirement.repository;

import com.tcu.projectpulse.requirement.domain.TraceabilityLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraceabilityLinkRepository extends JpaRepository<TraceabilityLink, Long> {

    List<TraceabilityLink> findByRequirementId(Long requirementId);
}
