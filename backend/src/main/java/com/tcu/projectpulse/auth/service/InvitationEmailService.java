package com.tcu.projectpulse.auth.service;

import com.tcu.projectpulse.auth.config.InvitationEmailProperties;
import com.tcu.projectpulse.auth.domain.Invitation;
import com.tcu.projectpulse.auth.domain.InvitationType;
import com.tcu.projectpulse.user.domain.UserAccount;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class InvitationEmailService {

    private final JavaMailSender mailSender;
    private final InvitationEmailProperties emailProperties;

    public InvitationEmailService(JavaMailSender mailSender, InvitationEmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
    }

    public InvitationDeliveryAttempt sendInvitation(Invitation invitation, UserAccount invitedBy) {
        String configurationError = validateConfiguration();
        if (configurationError != null) {
            return InvitationDeliveryAttempt.failed(configurationError);
        }

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(invitation.getEmail());
            helper.setFrom(emailProperties.fromAddress(), emailProperties.resolvedFromName());
            helper.setSubject("Welcome to Project Pulse - Complete Your Registration");
            helper.setText(buildBody(invitation, invitedBy), false);
            mailSender.send(message);
            return InvitationDeliveryAttempt.delivered("Invitation email sent to " + invitation.getEmail());
        } catch (MailException | MessagingException | UnsupportedEncodingException exception) {
            return InvitationDeliveryAttempt.failed("Invitation email delivery failed: " + exception.getMessage());
        }
    }

    public String registrationUrl(Invitation invitation) {
        if (!StringUtils.hasText(emailProperties.baseUrl())) {
            return null;
        }
        return buildRegistrationUrl(invitation);
    }

    private String validateConfiguration() {
        if (!emailProperties.enabled()) {
            return "Invitation email delivery is disabled. Set APP_EMAIL_ENABLED=true and configure SMTP settings.";
        }
        if (!StringUtils.hasText(emailProperties.baseUrl())) {
            return "Invitation email delivery requires APP_EMAIL_BASE_URL to be configured.";
        }
        if (!StringUtils.hasText(emailProperties.fromAddress())) {
            return "Invitation email delivery requires APP_EMAIL_FROM_ADDRESS to be configured.";
        }
        return null;
    }

    private String buildBody(Invitation invitation, UserAccount invitedBy) {
        String adminDisplayName = buildAdminDisplayName(invitedBy);
        String registrationUrl = registrationUrl(invitation);
        StringBuilder body = new StringBuilder()
                .append("Hello,\n\n")
                .append(adminDisplayName)
                .append(" has invited you to join Project Pulse");

        if (invitation.getType() == InvitationType.STUDENT && invitation.getSection() != null) {
            body.append(" for the ")
                    .append(invitation.getSection().getName())
                    .append(" senior design section");
        } else if (invitation.getType() == InvitationType.INSTRUCTOR) {
            body.append(" as an instructor");
        }

        body.append(".\n\n")
                .append(invitation.getMessage().trim())
                .append("\n\n")
                .append("To complete your registration, please use the link below:\n")
                .append(registrationUrl)
                .append("\n\n")
                .append("If you have any questions or need assistance, feel free to contact ")
                .append(invitedBy.getEmail())
                .append(" or our team directly.\n\n")
                .append("Please note: This email is not monitored, so do not reply directly to this message.\n\n")
                .append("Best regards,\n")
                .append(emailProperties.resolvedFromName());
        return body.toString();
    }

    private String buildRegistrationUrl(Invitation invitation) {
        String normalizedBaseUrl = emailProperties.baseUrl().endsWith("/")
                ? emailProperties.baseUrl().substring(0, emailProperties.baseUrl().length() - 1)
                : emailProperties.baseUrl();
        return UriComponentsBuilder.fromUriString(normalizedBaseUrl + "/login")
                .queryParam("mode", invitation.getType() == InvitationType.STUDENT
                        ? "student-register"
                        : "instructor-register")
                .queryParam("token", invitation.getToken())
                .build()
                .toUriString();
    }

    private String buildAdminDisplayName(UserAccount invitedBy) {
        String displayName = String.join(" ",
                invitedBy.getFirstName(),
                invitedBy.getLastName()
        ).trim();
        return StringUtils.hasText(displayName) ? displayName : invitedBy.getEmail();
    }
}
