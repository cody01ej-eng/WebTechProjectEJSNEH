package com.tcu.projectpulse.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.auth.domain.Invitation;
import com.tcu.projectpulse.auth.domain.InvitationStatus;
import com.tcu.projectpulse.auth.domain.InvitationType;
import com.tcu.projectpulse.auth.dto.InvitationBatchRequest;
import com.tcu.projectpulse.auth.dto.InvitationResponse;
import com.tcu.projectpulse.auth.repository.InvitationRepository;
import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.repository.SeniorDesignSectionRepository;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private SeniorDesignSectionRepository sectionRepository;

    @Mock
    private InvitationEmailService invitationEmailService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    private InvitationService invitationService;

    @BeforeEach
    void setUp() {
        invitationService = new InvitationService(
                invitationRepository,
                sectionRepository,
                invitationEmailService,
                authenticatedUserService
        );
    }

    @Test
    void inviteStudentsCreatesPendingInvitationsWhenEmailDeliverySucceeds() {
        SeniorDesignSection section = new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                null
        );
        UserAccount admin = new UserAccount(
                "Project",
                null,
                "Admin",
                "admin@projectpulse.local",
                "{noop}",
                UserRole.ADMIN,
                UserStatus.ACTIVE,
                null
        );
        InvitationBatchRequest request = new InvitationBatchRequest(
                1L,
                List.of("Student.One@tcu.edu", "student.two@tcu.edu"),
                "Use your registration link to complete setup."
        );
        AtomicLong ids = new AtomicLong(100L);

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(authenticatedUserService.requireCurrentUserAccount()).thenReturn(admin);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> {
            Invitation invitation = invocation.getArgument(0);
            if (invitation.getId() == null) {
                ReflectionTestUtils.setField(invitation, "id", ids.getAndIncrement());
            }
            return invitation;
        });
        when(invitationEmailService.sendInvitation(any(Invitation.class), any(UserAccount.class)))
                .thenReturn(InvitationDeliveryAttempt.delivered("sent"));

        List<InvitationResponse> response = invitationService.inviteStudents(request);

        assertThat(response).hasSize(2);
        assertThat(response).extracting(InvitationResponse::email)
                .containsExactly("student.one@tcu.edu", "student.two@tcu.edu");
        assertThat(response).extracting(InvitationResponse::status)
                .containsOnly(InvitationStatus.PENDING);
        verify(invitationEmailService, times(2)).sendInvitation(any(Invitation.class), any(UserAccount.class));
    }

    @Test
    void inviteStudentsMarksFailedDeliveriesWithoutRollingBackTheBatch() {
        SeniorDesignSection section = new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                null
        );
        UserAccount admin = new UserAccount(
                "Project",
                null,
                "Admin",
                "admin@projectpulse.local",
                "{noop}",
                UserRole.ADMIN,
                UserStatus.ACTIVE,
                null
        );
        InvitationBatchRequest request = new InvitationBatchRequest(
                1L,
                List.of("student.one@tcu.edu"),
                "Use your registration link to complete setup."
        );

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(authenticatedUserService.requireCurrentUserAccount()).thenReturn(admin);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> {
            Invitation invitation = invocation.getArgument(0);
            if (invitation.getId() == null) {
                ReflectionTestUtils.setField(invitation, "id", 100L);
            }
            return invitation;
        });
        when(invitationEmailService.sendInvitation(any(Invitation.class), any(UserAccount.class)))
                .thenReturn(InvitationDeliveryAttempt.failed("SMTP rejected the message"));

        List<InvitationResponse> response = invitationService.inviteStudents(request);

        assertThat(response).singleElement().extracting(InvitationResponse::status).isEqualTo(InvitationStatus.FAILED);
        verify(invitationRepository, times(2)).save(any(Invitation.class));
    }

    @Test
    void inviteStudentsThrowsInvalidArgumentWhenSectionIdIsNull() {
        InvitationBatchRequest request = new InvitationBatchRequest(
                null,
                List.of("student.one@tcu.edu"),
                "Use your registration link to complete setup."
        );

        assertThatThrownBy(() -> invitationService.inviteStudents(request))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("section id");
    }

    @Test
    void inviteStudentsThrowsResourceNotFoundWhenSectionDoesNotExist() {
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        InvitationBatchRequest request = new InvitationBatchRequest(
                99L,
                List.of("student.one@tcu.edu"),
                "Use your registration link to complete setup."
        );

        assertThatThrownBy(() -> invitationService.inviteStudents(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void inviteStudentsNormalizesEmailAddressesToLowercase() {
        SeniorDesignSection section = new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                null
        );
        UserAccount admin = new UserAccount(
                "Project",
                null,
                "Admin",
                "admin@projectpulse.local",
                "{noop}",
                UserRole.ADMIN,
                UserStatus.ACTIVE,
                null
        );
        InvitationBatchRequest request = new InvitationBatchRequest(
                1L,
                List.of("UPPER.CASE@TCU.EDU"),
                "Use your registration link to complete setup."
        );

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(authenticatedUserService.requireCurrentUserAccount()).thenReturn(admin);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> {
            Invitation invitation = invocation.getArgument(0);
            if (invitation.getId() == null) {
                ReflectionTestUtils.setField(invitation, "id", 100L);
            }
            return invitation;
        });
        when(invitationEmailService.sendInvitation(any(Invitation.class), any(UserAccount.class)))
                .thenReturn(InvitationDeliveryAttempt.delivered("sent"));

        List<InvitationResponse> response = invitationService.inviteStudents(request);

        assertThat(response).singleElement().extracting(InvitationResponse::email)
                .isEqualTo("upper.case@tcu.edu");
    }

    @Test
    void findPendingInvitationRejectsFailedInvitations() {
        Invitation invitation = new Invitation(
                "failed-token",
                "student.one@tcu.edu",
                InvitationType.STUDENT,
                InvitationStatus.FAILED,
                null,
                LocalDateTime.now().plusDays(7),
                "Use your registration link to complete setup."
        );

        when(invitationRepository.findByToken("failed-token")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.findPendingInvitation("failed-token", InvitationType.STUDENT))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Invitation delivery failed. Ask an admin to resend your invitation");
    }
}
