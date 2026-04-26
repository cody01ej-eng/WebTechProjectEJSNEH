package com.tcu.projectpulse.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.auth.domain.Invitation;
import com.tcu.projectpulse.auth.domain.InvitationStatus;
import com.tcu.projectpulse.auth.domain.InvitationType;
import com.tcu.projectpulse.auth.dto.InvitationBatchRequest;
import com.tcu.projectpulse.auth.dto.InvitationResponse;
import com.tcu.projectpulse.auth.repository.InvitationRepository;
import com.tcu.projectpulse.project.domain.Rubric;
import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.repository.RubricRepository;
import com.tcu.projectpulse.project.repository.SeniorDesignSectionRepository;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ActiveProfiles("test")
@Transactional
class InvitationServiceIntegrationTest {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private SeniorDesignSectionRepository sectionRepository;

    @Autowired
    private RubricRepository rubricRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @MockitoBean
    private InvitationEmailService invitationEmailService;

    @MockitoBean
    private AuthenticatedUserService authenticatedUserService;

    @Test
    void inviteStudentsPersistsNormalizedEmailsAndFailedDeliveryState() {
        SeniorDesignSection section = saveSection("2026-2027");
        UserAccount admin = saveAdmin("admin@projectpulse.local");

        when(authenticatedUserService.requireCurrentUserAccount()).thenReturn(admin);
        when(invitationEmailService.sendInvitation(any(Invitation.class), eq(admin))).thenAnswer(invocation -> {
            Invitation invitation = invocation.getArgument(0);
            if ("student.two@tcu.edu".equals(invitation.getEmail())) {
                return InvitationDeliveryAttempt.failed("SMTP rejected the message");
            }
            return InvitationDeliveryAttempt.delivered("sent");
        });

        List<InvitationResponse> response = invitationService.inviteStudents(new InvitationBatchRequest(
                section.getId(),
                List.of(" Student.One@tcu.edu ", " ", "student.two@tcu.edu"),
                "Use your registration link to complete setup."
        ));

        assertThat(response).extracting(InvitationResponse::email)
                .containsExactly("student.one@tcu.edu", "student.two@tcu.edu");
        assertThat(response).extracting(InvitationResponse::status)
                .containsExactly(InvitationStatus.PENDING, InvitationStatus.FAILED);

        Invitation deliveredInvitation = invitationRepository.findAll().stream()
                .filter(invitation -> invitation.getEmail().equals("student.one@tcu.edu"))
                .findFirst()
                .orElseThrow();
        Invitation failedInvitation = invitationRepository.findAll().stream()
                .filter(invitation -> invitation.getEmail().equals("student.two@tcu.edu"))
                .findFirst()
                .orElseThrow();

        assertThat(deliveredInvitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(deliveredInvitation.getSection().getId()).isEqualTo(section.getId());
        assertThat(failedInvitation.getStatus()).isEqualTo(InvitationStatus.FAILED);
        assertThat(failedInvitation.getMessage()).isEqualTo("Use your registration link to complete setup.");
    }

    @Test
    void findPendingInvitationExpiresPersistedRecords() {
        SeniorDesignSection section = saveSection("2027-2028");
        Invitation invitation = invitationRepository.save(new Invitation(
                "expired-token",
                "student.one@tcu.edu",
                InvitationType.STUDENT,
                InvitationStatus.PENDING,
                section,
                LocalDateTime.now().minusDays(1),
                "Use your registration link to complete setup."
        ));

        assertThatThrownBy(() -> invitationService.findPendingInvitation("expired-token", InvitationType.STUDENT))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Invitation has expired");

        Invitation expiredInvitation = invitationRepository.findById(invitation.getId()).orElseThrow();
        assertThat(expiredInvitation.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
    }

    private SeniorDesignSection saveSection(String name) {
        Rubric rubric = rubricRepository.save(new Rubric(name + " Rubric"));
        return sectionRepository.save(new SeniorDesignSection(
                name,
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                rubric
        ));
    }

    private UserAccount saveAdmin(String email) {
        return userAccountRepository.save(new UserAccount(
                "Project",
                null,
                "Admin",
                email,
                "{noop}password",
                UserRole.ADMIN,
                UserStatus.ACTIVE,
                null
        ));
    }
}
