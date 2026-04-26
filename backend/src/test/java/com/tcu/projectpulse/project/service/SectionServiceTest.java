package com.tcu.projectpulse.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.dto.CreateSectionRequest;
import com.tcu.projectpulse.project.repository.ActiveWeekRepository;
import com.tcu.projectpulse.project.repository.PeerEvaluationSubmissionRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignSectionRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignTeamRepository;
import com.tcu.projectpulse.project.repository.WeeklyActivityRepository;
import java.time.LocalDate;
import java.util.List;
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
}
