package com.tcu.projectpulse.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.project.repository.TeamInstructorAssignmentRepository;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.dto.InstructorDeactivationRequest;
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
}
