package com.tcu.projectpulse.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.project.domain.PeerEvaluationItem;
import com.tcu.projectpulse.project.domain.PeerEvaluationSubmission;
import com.tcu.projectpulse.project.domain.WeeklyActivity;
import com.tcu.projectpulse.project.repository.PeerEvaluationItemRepository;
import com.tcu.projectpulse.project.repository.PeerEvaluationSubmissionRepository;
import com.tcu.projectpulse.project.repository.TeamInstructorAssignmentRepository;
import com.tcu.projectpulse.project.repository.WeeklyActivityRepository;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.dto.InstructorDeactivationRequest;
import com.tcu.projectpulse.user.dto.StudentDeletionResponse;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private TeamInstructorAssignmentRepository teamInstructorAssignmentRepository;

    @Mock
    private WeeklyActivityRepository weeklyActivityRepository;

    @Mock
    private PeerEvaluationSubmissionRepository peerEvaluationSubmissionRepository;

    @Mock
    private PeerEvaluationItemRepository peerEvaluationItemRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void deactivateInstructorUpdatesTheInstructorStatus() {
        UserAccount instructor = new UserAccount(
                "Chris",
                null,
                "Lane",
                "chris@tcu.edu",
                "{noop}",
                UserRole.INSTRUCTOR,
                UserStatus.ACTIVE,
                null
        );
        ReflectionTestUtils.setField(instructor, "id", 4L);

        when(userAccountRepository.findById(4L)).thenReturn(Optional.of(instructor));
        when(teamInstructorAssignmentRepository.findByInstructorId(4L)).thenReturn(List.of());

        var response = userService.deactivateInstructor(4L, new InstructorDeactivationRequest("No longer teaching"));

        assertThat(response.status()).isEqualTo(UserStatus.DEACTIVATED);
    }

    @Test
    void reactivateInstructorRestoresTheInstructorStatus() {
        UserAccount instructor = new UserAccount(
                "Chris",
                null,
                "Lane",
                "chris@tcu.edu",
                "{noop}",
                UserRole.INSTRUCTOR,
                UserStatus.DEACTIVATED,
                null
        );
        ReflectionTestUtils.setField(instructor, "id", 5L);

        when(userAccountRepository.findById(5L)).thenReturn(Optional.of(instructor));
        when(teamInstructorAssignmentRepository.findByInstructorId(5L)).thenReturn(List.of());

        var response = userService.reactivateInstructor(5L);

        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void deleteStudentPhysicallyRemovesTheStudentAndAssociatedRecords() {
        UserAccount student = new UserAccount(
                "Jamie",
                null,
                "Stone",
                "jamie@tcu.edu",
                "{noop}",
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                null
        );
        ReflectionTestUtils.setField(student, "id", 8L);

        WeeklyActivity warActivity = org.mockito.Mockito.mock(WeeklyActivity.class);
        PeerEvaluationSubmission submission = org.mockito.Mockito.mock(PeerEvaluationSubmission.class);
        PeerEvaluationItem item = org.mockito.Mockito.mock(PeerEvaluationItem.class);

        when(userAccountRepository.findById(8L)).thenReturn(Optional.of(student));
        when(weeklyActivityRepository.findByStudentId(8L)).thenReturn(List.of(warActivity));
        when(peerEvaluationSubmissionRepository.findByAuthorId(8L)).thenReturn(List.of(submission));
        when(peerEvaluationItemRepository.findByEvaluateeId(8L)).thenReturn(List.of(item));

        StudentDeletionResponse response = userService.deleteStudent(8L);

        assertThat(response.studentId()).isEqualTo(8L);
        assertThat(response.studentName()).isEqualTo("Jamie Stone");
        assertThat(response.deletedWarActivities()).isEqualTo(1);
        assertThat(response.deletedPeerEvaluationSubmissions()).isEqualTo(1);
        assertThat(response.deletedPeerEvaluationItems()).isEqualTo(1);
        verify(weeklyActivityRepository).deleteAll(List.of(warActivity));
        verify(peerEvaluationSubmissionRepository).deleteAll(List.of(submission));
        verify(peerEvaluationItemRepository).deleteAll(List.of(item));
        verify(userAccountRepository).delete(student);
    }
}
