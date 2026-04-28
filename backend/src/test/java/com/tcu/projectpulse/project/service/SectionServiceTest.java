package com.tcu.projectpulse.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.project.domain.ActiveWeek;
import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.dto.ActiveWeekSetupRequest;
import com.tcu.projectpulse.project.dto.CreateSectionRequest;
import com.tcu.projectpulse.project.dto.SectionResponse;
import com.tcu.projectpulse.project.dto.UpdateSectionRequest;
import com.tcu.projectpulse.project.repository.ActiveWeekRepository;
import com.tcu.projectpulse.project.repository.PeerEvaluationSubmissionRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignSectionRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignTeamRepository;
import com.tcu.projectpulse.project.repository.WeeklyActivityRepository;
import com.tcu.projectpulse.shared.exception.ConflictException;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import java.time.LocalDate;
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
class SectionServiceTest {

    @Mock
    private SeniorDesignSectionRepository sectionRepository;

    @Mock
    private SeniorDesignTeamRepository teamRepository;

    @Mock
    private ActiveWeekRepository activeWeekRepository;

    @Mock
    private WeeklyActivityRepository weeklyActivityRepository;

    @Mock
    private PeerEvaluationSubmissionRepository submissionRepository;

    @Mock
    private RubricService rubricService;

    @InjectMocks
    private SectionService sectionService;

    // --- UC-4: createSection ---

    @Test
    void createSectionGeneratesWeeklyEntriesAcrossTheSectionCalendar() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        ReflectionTestUtils.setField(rubric, "id", 2L);

        when(sectionRepository.existsByNameIgnoreCase("2026-2027")).thenReturn(false);
        when(rubricService.getRubricEntity(2L)).thenReturn(rubric);
        when(sectionRepository.save(any(SeniorDesignSection.class))).thenAnswer(invocation -> {
            SeniorDesignSection section = invocation.getArgument(0);
            ReflectionTestUtils.setField(section, "id", 10L);
            return section;
        });
        when(sectionRepository.findById(10L)).thenAnswer(invocation -> {
            SeniorDesignSection section = new SeniorDesignSection(
                    "2026-2027",
                    LocalDate.of(2026, 8, 24),
                    LocalDate.of(2026, 9, 9),
                    rubric
            );
            ReflectionTestUtils.setField(section, "id", 10L);
            return java.util.Optional.of(section);
        });
        when(activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(10L)).thenReturn(List.of());
        when(teamRepository.findBySectionIdOrderByNameAsc(10L)).thenReturn(List.of());

        sectionService.createSection(new CreateSectionRequest(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2026, 9, 9),
                2L
        ));

        ArgumentCaptor<List> weeksCaptor = ArgumentCaptor.forClass(List.class);
        verify(activeWeekRepository).saveAll(weeksCaptor.capture());
        assertThat(weeksCaptor.getValue()).hasSize(3);
    }

    @Test
    void createSectionThrowsConflictWhenNameAlreadyExists() {
        when(sectionRepository.existsByNameIgnoreCase("Senior Design A")).thenReturn(true);

        assertThatThrownBy(() -> sectionService.createSection(new CreateSectionRequest(
                "Senior Design A",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                2L
        )))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Senior Design A");
    }

    @Test
    void createSectionThrowsInvalidArgumentWhenStartDateIsAfterEndDate() {
        assertThatThrownBy(() -> sectionService.createSection(new CreateSectionRequest(
                "2026-2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 8, 1),
                2L
        )))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("start date");
    }

    // --- UC-2: findSections ---

    @Test
    void findSectionsReturnsAllSectionsWhenNoNameFilterProvided() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        ReflectionTestUtils.setField(rubric, "id", 2L);
        SeniorDesignSection section = new SeniorDesignSection(
                "Senior Design A",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                rubric
        );
        ReflectionTestUtils.setField(section, "id", 10L);

        when(sectionRepository.findAll()).thenReturn(List.of(section));
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));
        when(activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(10L)).thenReturn(List.of());
        when(teamRepository.findBySectionIdOrderByNameAsc(10L)).thenReturn(List.of());

        List<SectionResponse> result = sectionService.findSections(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Senior Design A");
    }

    @Test
    void findSectionsFiltersResultsByCaseInsensitiveName() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        ReflectionTestUtils.setField(rubric, "id", 2L);

        SeniorDesignSection sectionA = new SeniorDesignSection(
                "Senior Design A",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                rubric
        );
        ReflectionTestUtils.setField(sectionA, "id", 10L);

        SeniorDesignSection sectionB = new SeniorDesignSection(
                "Senior Design B",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                rubric
        );
        ReflectionTestUtils.setField(sectionB, "id", 11L);

        when(sectionRepository.findAll()).thenReturn(List.of(sectionA, sectionB));
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(sectionA));
        when(activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(10L)).thenReturn(List.of());
        when(teamRepository.findBySectionIdOrderByNameAsc(10L)).thenReturn(List.of());

        List<SectionResponse> result = sectionService.findSections("design a");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
    }

    // --- UC-3: getSection ---

    @Test
    void getSectionThrowsResourceNotFoundWhenSectionDoesNotExist() {
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.getSection(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- UC-5: updateSection ---

    @Test
    void updateSectionUpdatesNameWhenDatesAreUnchanged() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        ReflectionTestUtils.setField(rubric, "id", 2L);

        SeniorDesignSection section = new SeniorDesignSection(
                "Senior Design A",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                rubric
        );
        ReflectionTestUtils.setField(section, "id", 10L);

        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));
        when(sectionRepository.findByNameIgnoreCase("Renamed Section")).thenReturn(Optional.empty());
        when(rubricService.getRubricEntity(2L)).thenReturn(rubric);
        when(activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(10L)).thenReturn(List.of());
        when(teamRepository.findBySectionIdOrderByNameAsc(10L)).thenReturn(List.of());

        SectionResponse result = sectionService.updateSection(10L, new UpdateSectionRequest(
                "Renamed Section",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                2L
        ));

        assertThat(result.name()).isEqualTo("Renamed Section");
    }

    @Test
    void updateSectionThrowsConflictWhenNameIsTakenByAnotherSection() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        ReflectionTestUtils.setField(rubric, "id", 2L);

        SeniorDesignSection sectionA = new SeniorDesignSection(
                "Senior Design A",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                rubric
        );
        ReflectionTestUtils.setField(sectionA, "id", 10L);

        SeniorDesignSection sectionB = new SeniorDesignSection(
                "Senior Design B",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                rubric
        );
        ReflectionTestUtils.setField(sectionB, "id", 11L);

        when(sectionRepository.findById(10L)).thenReturn(Optional.of(sectionA));
        when(sectionRepository.findByNameIgnoreCase("Senior Design B")).thenReturn(Optional.of(sectionB));

        assertThatThrownBy(() -> sectionService.updateSection(10L, new UpdateSectionRequest(
                "Senior Design B",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                2L
        )))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Senior Design B");
    }

    @Test
    void updateSectionThrowsInvalidArgumentWhenDatesChangedAfterActivityExists() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        ReflectionTestUtils.setField(rubric, "id", 2L);

        SeniorDesignSection section = new SeniorDesignSection(
                "Senior Design A",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                rubric
        );
        ReflectionTestUtils.setField(section, "id", 10L);

        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));
        when(sectionRepository.findByNameIgnoreCase("Senior Design A")).thenReturn(Optional.of(section));
        when(weeklyActivityRepository.existsByTeamSectionId(10L)).thenReturn(true);

        assertThatThrownBy(() -> sectionService.updateSection(10L, new UpdateSectionRequest(
                "Senior Design A",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 5, 1),
                2L
        )))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("dates cannot be changed");
    }

    // --- UC-6: configureActiveWeeks ---

    @Test
    void configureActiveWeeksMarksSpecifiedWeeksAsInactive() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        ReflectionTestUtils.setField(rubric, "id", 2L);

        SeniorDesignSection section = new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2026, 9, 9),
                rubric
        );
        ReflectionTestUtils.setField(section, "id", 10L);

        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));
        when(activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(10L)).thenReturn(List.of());
        when(teamRepository.findBySectionIdOrderByNameAsc(10L)).thenReturn(List.of());

        sectionService.configureActiveWeeks(10L, new ActiveWeekSetupRequest(
                List.of(LocalDate.of(2026, 8, 31))
        ));

        ArgumentCaptor<List> weeksCaptor = ArgumentCaptor.forClass(List.class);
        verify(activeWeekRepository).saveAll(weeksCaptor.capture());

        List<ActiveWeek> savedWeeks = (List<ActiveWeek>) weeksCaptor.getValue();
        assertThat(savedWeeks).hasSize(3);

        ActiveWeek inactiveWeek = savedWeeks.stream()
                .filter(w -> w.getWeekStartDate().equals(LocalDate.of(2026, 8, 31)))
                .findFirst()
                .orElseThrow();
        assertThat(inactiveWeek.isActive()).isFalse();

        assertThat(savedWeeks.stream()
                .filter(w -> !w.getWeekStartDate().equals(LocalDate.of(2026, 8, 31)))
                .allMatch(ActiveWeek::isActive))
                .isTrue();
    }

    @Test
    void configureActiveWeeksThrowsWhenInactiveDateIsOutsideSectionCalendar() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        ReflectionTestUtils.setField(rubric, "id", 2L);

        SeniorDesignSection section = new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2026, 9, 9),
                rubric
        );
        ReflectionTestUtils.setField(section, "id", 10L);

        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));
        when(activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> sectionService.configureActiveWeeks(10L, new ActiveWeekSetupRequest(
                List.of(LocalDate.of(2026, 12, 21))
        )))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Inactive week dates must fall within the section calendar");
    }
}
