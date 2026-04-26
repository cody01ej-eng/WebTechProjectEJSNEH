package com.tcu.projectpulse.project.service;

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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SectionService {

    private final SeniorDesignSectionRepository sectionRepository;
    private final SeniorDesignTeamRepository teamRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final WeeklyActivityRepository weeklyActivityRepository;
    private final PeerEvaluationSubmissionRepository submissionRepository;
    private final RubricService rubricService;

    public SectionService(SeniorDesignSectionRepository sectionRepository,
                          SeniorDesignTeamRepository teamRepository,
                          ActiveWeekRepository activeWeekRepository,
                          WeeklyActivityRepository weeklyActivityRepository,
                          PeerEvaluationSubmissionRepository submissionRepository,
                          RubricService rubricService) {
        this.sectionRepository = sectionRepository;
        this.teamRepository = teamRepository;
        this.activeWeekRepository = activeWeekRepository;
        this.weeklyActivityRepository = weeklyActivityRepository;
        this.submissionRepository = submissionRepository;
        this.rubricService = rubricService;
    }

    public SectionResponse createSection(CreateSectionRequest request) {
        validateSectionDates(request.startDate(), request.endDate());
        if (sectionRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Section name already exists: " + request.name());
        }
        Rubric rubric = rubricService.getRubricEntity(request.rubricId());
        SeniorDesignSection section = sectionRepository.save(new SeniorDesignSection(
                request.name(),
                request.startDate(),
                request.endDate(),
                rubric
        ));
        regenerateWeeks(section, Set.of());
        return getSection(section.getId());
    }

    public SectionResponse configureActiveWeeks(Long sectionId, ActiveWeekSetupRequest request) {
        SeniorDesignSection section = getSectionEntity(sectionId);
        regenerateWeeks(section, Set.copyOf(request.inactiveWeekStartDates()));
        return getSection(sectionId);
    }

    public SectionResponse updateSection(Long sectionId, UpdateSectionRequest request) {
        validateSectionDates(request.startDate(), request.endDate());
        SeniorDesignSection section = getSectionEntity(sectionId);
        sectionRepository.findByNameIgnoreCase(request.name())
                .filter(existing -> !existing.getId().equals(sectionId))
                .ifPresent(existing -> {
                    throw new ConflictException("Section name already exists: " + request.name());
                });

        boolean datesChanged = !section.getStartDate().equals(request.startDate())
                || !section.getEndDate().equals(request.endDate());
        if (datesChanged && hasSectionActivity(sectionId)) {
            throw new InvalidArgumentException(
                    "Section dates cannot be changed after WAR or peer evaluation data exists");
        }

        Set<LocalDate> inactiveWeekStartDates = activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(sectionId)
                .stream()
                .filter(week -> !week.isActive())
                .map(ActiveWeek::getWeekStartDate)
                .collect(Collectors.toSet());

        Rubric rubric = rubricService.getRubricEntity(request.rubricId());
        section.update(request.name(), request.startDate(), request.endDate(), rubric);
        if (datesChanged) {
            regenerateWeeks(section, retainWeeksWithinSection(section, inactiveWeekStartDates));
        }
        return getSection(sectionId);
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> findSections(String name) {
        String normalizedName = name == null ? "" : name.trim().toLowerCase();
        return sectionRepository.findAll().stream()
                .filter(section -> normalizedName.isBlank() || section.getName().toLowerCase().contains(normalizedName))
                .map(section -> getSection(section.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SectionResponse getSection(Long sectionId) {
        SeniorDesignSection section = getSectionEntity(sectionId);
        return new SectionResponse(
                section.getId(),
                section.getName(),
                section.getStartDate(),
                section.getEndDate(),
                section.getRubric().getId(),
                section.getRubric().getName(),
                activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(sectionId).stream()
                        .map(week -> new SectionResponse.WeekView(week.getId(), week.getWeekStartDate(), week.isActive()))
                        .toList(),
                teamRepository.findBySectionIdOrderByNameAsc(sectionId).stream()
                        .map(team -> new SectionResponse.TeamView(team.getId(), team.getName()))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public SeniorDesignSection getSectionEntity(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id " + sectionId));
    }

    @Transactional(readOnly = true)
    public ActiveWeek getWeek(Long sectionId, LocalDate weekStartDate) {
        return activeWeekRepository.findBySectionIdAndWeekStartDate(sectionId, weekStartDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Week " + weekStartDate + " is not configured for section " + sectionId));
    }

    private boolean hasSectionActivity(Long sectionId) {
        return weeklyActivityRepository.existsByTeamSectionId(sectionId)
                || submissionRepository.existsByTeamSectionId(sectionId);
    }

    private void regenerateWeeks(SeniorDesignSection section, Set<LocalDate> inactiveWeekStartDates) {
        activeWeekRepository.deleteAll(activeWeekRepository.findBySectionIdOrderByWeekStartDateAsc(section.getId()));

        List<ActiveWeek> weeks = enumerateWeeks(section).stream()
                .map(weekStart -> new ActiveWeek(section, weekStart, !inactiveWeekStartDates.contains(weekStart)))
                .toList();

        Set<LocalDate> generatedWeeks = weeks.stream().map(ActiveWeek::getWeekStartDate).collect(Collectors.toSet());
        if (!generatedWeeks.containsAll(inactiveWeekStartDates)) {
            throw new InvalidArgumentException("Inactive week dates must fall within the section calendar");
        }

        activeWeekRepository.saveAll(weeks);
    }

    private Set<LocalDate> retainWeeksWithinSection(SeniorDesignSection section, Set<LocalDate> inactiveWeekStartDates) {
        Set<LocalDate> validWeeks = enumerateWeeks(section).stream().collect(Collectors.toSet());
        return inactiveWeekStartDates.stream()
                .filter(validWeeks::contains)
                .collect(Collectors.toSet());
    }

    private List<LocalDate> enumerateWeeks(SeniorDesignSection section) {
        LocalDate firstWeek = section.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastWeek = section.getEndDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return firstWeek.datesUntil(lastWeek.plusWeeks(1), java.time.Period.ofWeeks(1)).toList();
    }

    private void validateSectionDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidArgumentException("Section start date must be on or before end date");
        }
    }
}
