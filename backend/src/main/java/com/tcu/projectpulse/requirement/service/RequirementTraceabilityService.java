package com.tcu.projectpulse.requirement.service;

import com.tcu.projectpulse.requirement.domain.RequirementReference;
import com.tcu.projectpulse.requirement.domain.TraceabilityLink;
import com.tcu.projectpulse.requirement.dto.CreateRequirementReferenceRequest;
import com.tcu.projectpulse.requirement.dto.CreateTraceabilityLinkRequest;
import com.tcu.projectpulse.requirement.dto.RequirementReferenceResponse;
import com.tcu.projectpulse.requirement.dto.TraceabilityLinkResponse;
import com.tcu.projectpulse.requirement.repository.RequirementReferenceRepository;
import com.tcu.projectpulse.requirement.repository.TraceabilityLinkRepository;
import com.tcu.projectpulse.shared.exception.ConflictException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequirementTraceabilityService {

    private final RequirementReferenceRepository requirementReferenceRepository;
    private final TraceabilityLinkRepository traceabilityLinkRepository;

    public RequirementTraceabilityService(RequirementReferenceRepository requirementReferenceRepository,
                                          TraceabilityLinkRepository traceabilityLinkRepository) {
        this.requirementReferenceRepository = requirementReferenceRepository;
        this.traceabilityLinkRepository = traceabilityLinkRepository;
    }

    public RequirementReferenceResponse createRequirement(CreateRequirementReferenceRequest request) {
        if (requirementReferenceRepository.existsByReferenceKeyIgnoreCase(request.referenceKey())) {
            throw new ConflictException("Requirement reference already exists: " + request.referenceKey());
        }
        RequirementReference reference = requirementReferenceRepository.save(new RequirementReference(
                request.referenceKey(),
                request.title(),
                request.source(),
                request.summary()
        ));
        return toResponse(reference);
    }

    public List<RequirementReferenceResponse> findRequirements() {
        return requirementReferenceRepository.findAll().stream()
                .sorted(Comparator.comparing(RequirementReference::getReferenceKey))
                .map(this::toResponse)
                .toList();
    }

    public TraceabilityLinkResponse createTraceabilityLink(CreateTraceabilityLinkRequest request) {
        RequirementReference reference = requirementReferenceRepository.findById(request.requirementId())
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id " + request.requirementId()));
        TraceabilityLink link = traceabilityLinkRepository.save(new TraceabilityLink(
                reference,
                request.componentType(),
                request.componentName(),
                request.status(),
                request.notes()
        ));
        return toResponse(link);
    }

    public List<TraceabilityLinkResponse> findTraceabilityLinks() {
        return traceabilityLinkRepository.findAll().stream()
                .sorted(Comparator.comparing(link -> link.getRequirement().getReferenceKey()))
                .map(this::toResponse)
                .toList();
    }

    private RequirementReferenceResponse toResponse(RequirementReference reference) {
        return new RequirementReferenceResponse(
                reference.getId(),
                reference.getReferenceKey(),
                reference.getTitle(),
                reference.getSource(),
                reference.getSummary()
        );
    }

    private TraceabilityLinkResponse toResponse(TraceabilityLink link) {
        return new TraceabilityLinkResponse(
                link.getId(),
                link.getRequirement().getId(),
                link.getRequirement().getReferenceKey(),
                link.getRequirement().getTitle(),
                link.getComponentType(),
                link.getComponentName(),
                link.getStatus(),
                link.getNotes()
        );
    }
}
