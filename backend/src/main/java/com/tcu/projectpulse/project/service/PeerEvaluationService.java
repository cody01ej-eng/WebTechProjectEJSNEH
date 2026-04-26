package com.tcu.projectpulse.project.service;

import com.tcu.projectpulse.auth.service.AuthenticatedUserService;
import com.tcu.projectpulse.project.domain.ActiveWeek;
import com.tcu.projectpulse.project.domain.PeerEvaluationCriterionScore;
import com.tcu.projectpulse.project.domain.PeerEvaluationItem;
import com.tcu.projectpulse.project.domain.PeerEvaluationSubmission;
import com.tcu.projectpulse.project.domain.RubricCriterion;
import com.tcu.projectpulse.project.dto.PeerEvaluationItemRequest;
import com.tcu.projectpulse.project.dto.PeerEvaluationScoreRequest;
import com.tcu.projectpulse.project.dto.PeerEvaluationSubmissionResponse;
import com.tcu.projectpulse.project.dto.SubmitPeerEvaluationRequest;
import com.tcu.projectpulse.project.repository.PeerEvaluationSubmissionRepository;
import com.tcu.projectpulse.shared.exception.ConflictException;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PeerEvaluationService {

    private final PeerEvaluationSubmissionRepository submissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final SectionService sectionService;
    private final Clock clock;
    private final AuthenticatedUserService authenticatedUserService;

    public PeerEvaluationService(PeerEvaluationSubmissionRepository submissionRepository,
                                 UserAccountRepository userAccountRepository,
                                 SectionService sectionService,
                                 Clock clock,
                                 AuthenticatedUserService authenticatedUserService) {
        this.submissionRepository = submissionRepository;
        this.userAccountRepository = userAccountRepository;
        this.sectionService = sectionService;
        this.clock = clock;
        this.authenticatedUserService = authenticatedUserService;
    }

    public PeerEvaluationSubmissionResponse submitEvaluation(SubmitPeerEvaluationRequest request) {
        if (!authenticatedUserService.requireCurrentUserId().equals(request.authorId())) {
            throw new AccessDeniedException("Students can only submit their own peer evaluation");
        }
        UserAccount author = getStudent(request.authorId());
        LocalDate expectedWeekStart = LocalDate.now(clock)
                .minusWeeks(1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (!expectedWeekStart.equals(request.weekStartDate())) {
            throw new InvalidArgumentException("Peer evaluations can only be submitted for the previous week");
        }

        ActiveWeek activeWeek = sectionService.getWeek(author.getSection().getId(), request.weekStartDate());
        if (!activeWeek.isActive()) {
            throw new InvalidArgumentException("Peer evaluations can only be submitted during active weeks");
        }
        if (submissionRepository.existsByAuthorIdAndActiveWeekWeekStartDate(author.getId(), request.weekStartDate())) {
            throw new ConflictException("Peer evaluation has already been submitted for this week");
        }

        Map<Long, UserAccount> teamStudents = userAccountRepository.findByAssignedTeamId(author.getAssignedTeam().getId())
                .stream()
                .filter(user -> user.getRole() == UserRole.STUDENT)
                .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        Set<Long> requestedEvaluatees = request.evaluations().stream()
                .map(PeerEvaluationItemRequest::evaluateeId)
                .collect(Collectors.toSet());
        if (!requestedEvaluatees.equals(teamStudents.keySet())) {
            throw new InvalidArgumentException("Every team member must be evaluated exactly once");
        }

        Map<Long, RubricCriterion> criteriaById = author.getSection().getRubric().getCriteria().stream()
                .collect(Collectors.toMap(RubricCriterion::getId, Function.identity()));

        PeerEvaluationSubmission submission = new PeerEvaluationSubmission(author, author.getAssignedTeam(), activeWeek);
        for (PeerEvaluationItemRequest itemRequest : request.evaluations()) {
            UserAccount evaluatee = teamStudents.get(itemRequest.evaluateeId());
            PeerEvaluationItem item = new PeerEvaluationItem(
                    submission,
                    evaluatee,
                    itemRequest.publicComment(),
                    itemRequest.privateComment()
            );
            validateScoreSet(itemRequest.scores(), criteriaById);
            for (PeerEvaluationScoreRequest scoreRequest : itemRequest.scores()) {
                RubricCriterion criterion = criteriaById.get(scoreRequest.criterionId());
                if (scoreRequest.score() < 1 || criterion.getMaxScore().compareTo(java.math.BigDecimal.valueOf(scoreRequest.score())) < 0) {
                    throw new InvalidArgumentException("Score is out of range for criterion " + criterion.getName());
                }
                item.addCriterionScore(new PeerEvaluationCriterionScore(item, criterion, scoreRequest.score()));
            }
            submission.addItem(item);
        }

        return toResponse(submissionRepository.save(submission));
    }

    public PeerEvaluationSubmissionResponse toResponse(PeerEvaluationSubmission submission) {
        return new PeerEvaluationSubmissionResponse(
                submission.getId(),
                submission.getAuthor().getId(),
                fullName(submission.getAuthor()),
                submission.getTeam().getId(),
                submission.getTeam().getName(),
                submission.getActiveWeek().getWeekStartDate(),
                submission.getItems().stream()
                        .sorted(Comparator.comparing(item -> item.getEvaluatee().getLastName()))
                        .map(item -> new PeerEvaluationSubmissionResponse.EvaluationView(
                                item.getEvaluatee().getId(),
                                fullName(item.getEvaluatee()),
                                item.getPublicComment(),
                                item.getPrivateComment(),
                                item.getCriterionScores().stream()
                                        .sorted(Comparator.comparing(score -> score.getCriterion().getDisplayOrder()))
                                        .map(score -> new PeerEvaluationSubmissionResponse.ScoreView(
                                                score.getCriterion().getId(),
                                                score.getCriterion().getName(),
                                                score.getScore()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }

    private void validateScoreSet(java.util.List<PeerEvaluationScoreRequest> scores, Map<Long, RubricCriterion> criteriaById) {
        Set<Long> requestedCriterionIds = scores.stream()
                .map(PeerEvaluationScoreRequest::criterionId)
                .collect(Collectors.toSet());
        if (!requestedCriterionIds.equals(criteriaById.keySet())) {
            throw new InvalidArgumentException("Every rubric criterion must be scored exactly once");
        }
    }

    private UserAccount getStudent(Long studentId) {
        UserAccount student = userAccountRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));
        if (student.getRole() != UserRole.STUDENT) {
            throw new InvalidArgumentException("User " + studentId + " is not a student");
        }
        if (student.getAssignedTeam() == null || student.getSection() == null) {
            throw new InvalidArgumentException("Student must be assigned to a team before submitting peer evaluations");
        }
        return student;
    }

    private String fullName(UserAccount user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
