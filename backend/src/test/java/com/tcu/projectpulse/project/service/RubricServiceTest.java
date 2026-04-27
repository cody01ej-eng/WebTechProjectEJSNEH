package com.tcu.projectpulse.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.domain.RubricCriterion;
import com.tcu.projectpulse.project.dto.CreateRubricRequest;
import com.tcu.projectpulse.project.dto.RubricCriterionRequest;
import com.tcu.projectpulse.project.dto.RubricResponse;
import com.tcu.projectpulse.project.repository.RubricRepository;
import com.tcu.projectpulse.shared.exception.ConflictException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RubricServiceTest {

    @Mock
    private RubricRepository rubricRepository;

    @InjectMocks
    private RubricService rubricService;

    @Test
    void createRubricReturnsSavedRubricWithCriteria() {
        when(rubricRepository.existsByNameIgnoreCase("Peer Eval Rubric v1")).thenReturn(false);
        when(rubricRepository.save(any(Rubric.class))).thenAnswer(invocation -> {
            Rubric rubric = invocation.getArgument(0);
            ReflectionTestUtils.setField(rubric, "id", 1L);
            for (int i = 0; i < rubric.getCriteria().size(); i++) {
                ReflectionTestUtils.setField(rubric.getCriteria().get(i), "id", (long) (i + 1));
            }
            return rubric;
        });

        RubricResponse response = rubricService.createRubric(new CreateRubricRequest(
                "Peer Eval Rubric v1",
                List.of(
                        new RubricCriterionRequest("Quality of work", "Does high quality work", BigDecimal.valueOf(10)),
                        new RubricCriterionRequest("Productivity", "Gets things done on time", BigDecimal.valueOf(10))
                )
        ));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Peer Eval Rubric v1");
        assertThat(response.criteria()).hasSize(2);
        assertThat(response.criteria().get(0).name()).isEqualTo("Quality of work");
        assertThat(response.criteria().get(1).name()).isEqualTo("Productivity");
    }

    @Test
    void createRubricThrowsConflictWhenNameAlreadyExists() {
        when(rubricRepository.existsByNameIgnoreCase("Peer Eval Rubric v1")).thenReturn(true);

        assertThatThrownBy(() -> rubricService.createRubric(new CreateRubricRequest(
                "Peer Eval Rubric v1",
                List.of(new RubricCriterionRequest("Quality", "Quality criterion", BigDecimal.valueOf(5)))
        )))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Peer Eval Rubric v1");
    }

    @Test
    void createRubricAssignsDisplayOrderStartingAtOneForSingleCriterion() {
        when(rubricRepository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(rubricRepository.save(any(Rubric.class))).thenAnswer(invocation -> {
            Rubric rubric = invocation.getArgument(0);
            ReflectionTestUtils.setField(rubric, "id", 1L);
            ReflectionTestUtils.setField(rubric.getCriteria().get(0), "id", 1L);
            return rubric;
        });

        rubricService.createRubric(new CreateRubricRequest(
                "Single Criterion Rubric",
                List.of(new RubricCriterionRequest("Quality", "Quality criterion", BigDecimal.valueOf(5)))
        ));

        ArgumentCaptor<Rubric> captor = ArgumentCaptor.forClass(Rubric.class);
        verify(rubricRepository).save(captor.capture());
        assertThat(captor.getValue().getCriteria().get(0).getDisplayOrder()).isEqualTo(1);
    }

    @Test
    void createRubricAssignsIncrementalDisplayOrderForMultipleCriteria() {
        when(rubricRepository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(rubricRepository.save(any(Rubric.class))).thenAnswer(invocation -> {
            Rubric rubric = invocation.getArgument(0);
            ReflectionTestUtils.setField(rubric, "id", 1L);
            for (int i = 0; i < rubric.getCriteria().size(); i++) {
                ReflectionTestUtils.setField(rubric.getCriteria().get(i), "id", (long) (i + 1));
            }
            return rubric;
        });

        rubricService.createRubric(new CreateRubricRequest(
                "Multi Criterion Rubric",
                List.of(
                        new RubricCriterionRequest("First", "First criterion", BigDecimal.valueOf(5)),
                        new RubricCriterionRequest("Second", "Second criterion", BigDecimal.valueOf(5)),
                        new RubricCriterionRequest("Third", "Third criterion", BigDecimal.valueOf(5))
                )
        ));

        ArgumentCaptor<Rubric> captor = ArgumentCaptor.forClass(Rubric.class);
        verify(rubricRepository).save(captor.capture());
        List<RubricCriterion> criteria = captor.getValue().getCriteria();
        assertThat(criteria.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(criteria.get(1).getDisplayOrder()).isEqualTo(2);
        assertThat(criteria.get(2).getDisplayOrder()).isEqualTo(3);
    }

    @Test
    void findAllReturnsEmptyListWhenNoRubricsExist() {
        when(rubricRepository.findAll()).thenReturn(List.of());

        assertThat(rubricService.findAll()).isEmpty();
    }

    @Test
    void findAllReturnsRubricsSortedByName() {
        Rubric rubricB = new Rubric("Beta Rubric");
        ReflectionTestUtils.setField(rubricB, "id", 2L);
        Rubric rubricA = new Rubric("Alpha Rubric");
        ReflectionTestUtils.setField(rubricA, "id", 1L);
        when(rubricRepository.findAll()).thenReturn(List.of(rubricB, rubricA));

        List<RubricResponse> result = rubricService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Alpha Rubric");
        assertThat(result.get(1).name()).isEqualTo("Beta Rubric");
    }

    @Test
    void getRubricEntityReturnsRubricWhenFound() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        ReflectionTestUtils.setField(rubric, "id", 1L);
        when(rubricRepository.findById(1L)).thenReturn(Optional.of(rubric));

        Rubric result = rubricService.getRubricEntity(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Peer Eval Rubric v1");
    }

    @Test
    void getRubricEntityThrowsResourceNotFoundWhenIdIsMissing() {
        when(rubricRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rubricService.getRubricEntity(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
