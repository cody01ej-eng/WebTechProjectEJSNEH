package com.tcu.projectpulse.project.service;

import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.domain.RubricCriterion;
import com.tcu.projectpulse.project.dto.CreateRubricRequest;
import com.tcu.projectpulse.project.dto.RubricResponse;
import com.tcu.projectpulse.project.repository.RubricRepository;
import com.tcu.projectpulse.shared.exception.ConflictException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RubricService {

    private final RubricRepository rubricRepository;

    public RubricService(RubricRepository rubricRepository) {
        this.rubricRepository = rubricRepository;
    }

    public RubricResponse createRubric(CreateRubricRequest request) {
        if (rubricRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Rubric name already exists: " + request.name());
        }

        Rubric rubric = new Rubric(request.name());
        for (int index = 0; index < request.criteria().size(); index++) {
            var criterionRequest = request.criteria().get(index);
            rubric.addCriterion(new RubricCriterion(
                    rubric,
                    criterionRequest.name(),
                    criterionRequest.description(),
                    criterionRequest.maxScore(),
                    index + 1
            ));
        }
        return toResponse(rubricRepository.save(rubric));
    }

    @Transactional(readOnly = true)
    public List<RubricResponse> findAll() {
        return rubricRepository.findAll().stream()
                .sorted(Comparator.comparing(Rubric::getName))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Rubric getRubricEntity(Long rubricId) {
        return rubricRepository.findById(rubricId)
                .orElseThrow(() -> new ResourceNotFoundException("Rubric not found with id " + rubricId));
    }

    public RubricResponse toResponse(Rubric rubric) {
        return new RubricResponse(
                rubric.getId(),
                rubric.getName(),
                rubric.getCriteria().stream()
                        .sorted(Comparator.comparing(RubricCriterion::getDisplayOrder))
                        .map(criterion -> new RubricResponse.Criterion(
                                criterion.getId(),
                                criterion.getName(),
                                criterion.getDescription(),
                                criterion.getMaxScore(),
                                criterion.getDisplayOrder()
                        ))
                        .toList()
        );
    }
}
