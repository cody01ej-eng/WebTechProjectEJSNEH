package com.tcu.projectpulse.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcu.projectpulse.auth.config.InvitationEmailProperties;
import com.tcu.projectpulse.auth.domain.Invitation;
import com.tcu.projectpulse.auth.domain.InvitationStatus;
import com.tcu.projectpulse.auth.domain.InvitationType;
import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class InvitationEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private InvitationEmailService invitationEmailService;

    @BeforeEach
    void setUp() {
        invitationEmailService = new InvitationEmailService(
                mailSender,
                new InvitationEmailProperties(
                        true,
                        "https://projectpulse.example.com",
                        "noreply@projectpulse.example.com",
                        "Project Pulse Team"
                )
        );
    }

    @Test
    void sendInvitationBuildsADeepLinkedRegistrationEmail() throws Exception {
        SeniorDesignSection section = new SeniorDesignSection(
                "2026-2027",
                LocalDate.of(2026, 8, 24),
                LocalDate.of(2027, 5, 1),
                null
        );
        Invitation invitation = new Invitation(
                "student-token",
                "student.one@tcu.edu",
                InvitationType.STUDENT,
                InvitationStatus.PENDING,
                section,
                LocalDateTime.of(2026, 5, 23, 12, 0),
                "Use this invitation to complete your student account setup."
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
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        InvitationDeliveryAttempt response = invitationEmailService.sendInvitation(invitation, admin);

        assertThat(response.delivered()).isTrue();
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getSubject()).isEqualTo("Welcome to Project Pulse - Complete Your Registration");
        assertThat(sentMessage.getAllRecipients()[0].toString()).isEqualTo("student.one@tcu.edu");
        assertThat(sentMessage.getContent().toString())
                .contains("Project Admin has invited you to join Project Pulse for the 2026-2027 senior design section.")
                .contains("https://projectpulse.example.com/login?mode=student-register&token=student-token")
                .contains("admin@projectpulse.local");
    }

    @Test
    void sendInvitationReturnsFailureWhenEmailDeliveryIsDisabled() {
        InvitationEmailService disabledService = new InvitationEmailService(
                mailSender,
                new InvitationEmailProperties(false, "http://localhost:5173", "noreply@projectpulse.local", "Project Pulse Team")
        );
        Invitation invitation = new Invitation(
                "instructor-token",
                "instructor.one@tcu.edu",
                InvitationType.INSTRUCTOR,
                InvitationStatus.PENDING,
                null,
                LocalDateTime.of(2026, 5, 23, 12, 0),
                "Use this invitation to complete your instructor account setup."
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

        InvitationDeliveryAttempt response = disabledService.sendInvitation(invitation, admin);

        assertThat(response.delivered()).isFalse();
        assertThat(response.detail()).contains("APP_EMAIL_ENABLED=true");
    }
}
