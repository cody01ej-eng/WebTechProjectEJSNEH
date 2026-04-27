package com.tcu.projectpulse.auth.service;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final SeniorDesignSectionRepository sectionRepository;
    private final InvitationEmailService invitationEmailService;
    private final AuthenticatedUserService authenticatedUserService;

    public InvitationService(InvitationRepository invitationRepository,
                             SeniorDesignSectionRepository sectionRepository,
                             InvitationEmailService invitationEmailService,
                             AuthenticatedUserService authenticatedUserService) {
        this.invitationRepository = invitationRepository;
        this.sectionRepository = sectionRepository;
        this.invitationEmailService = invitationEmailService;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<InvitationResponse> inviteStudents(InvitationBatchRequest request) {
        if (request.sectionId() == null) {
            throw new InvalidArgumentException("Student invitations must include a section id");
        }
        SeniorDesignSection section = sectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id " + request.sectionId()));
        return createInvitations(request, InvitationType.STUDENT, section);
    }

    public List<InvitationResponse> inviteInstructors(InvitationBatchRequest request) {
        return createInvitations(request, InvitationType.INSTRUCTOR, null);
    }

    private List<InvitationResponse> createInvitations(InvitationBatchRequest request,
                                                       InvitationType type,
                                                       SeniorDesignSection section) {
        UserAccount invitedBy = authenticatedUserService.requireCurrentUserAccount();
        List<InvitationResponse> responses = new ArrayList<>();

        for (String rawEmail : request.emails()) {
            String email = rawEmail.trim().toLowerCase();
            if (email.isBlank()) {
                continue;
            }

            Invitation invitation = invitationRepository.save(new Invitation(
                    UUID.randomUUID().toString(),
                    email,
                    type,
                    InvitationStatus.PENDING,
                    section,
                    LocalDateTime.now().plusDays(30),
                    request.message()
            ));

            InvitationDeliveryAttempt deliveryAttempt = invitationEmailService.sendInvitation(invitation, invitedBy);
            if (!deliveryAttempt.delivered()) {
                invitation.fail();
                invitationRepository.save(invitation);
            }

            responses.add(toResponse(invitation));
        }

        return responses;
    }

    public Invitation findPendingInvitation(String token, InvitationType expectedType) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        if (invitation.getType() != expectedType) {
            throw new InvalidArgumentException("Invitation type does not match registration flow");
        }
        if (invitation.getStatus() == InvitationStatus.FAILED) {
            throw new InvalidArgumentException("Invitation delivery failed. Ask an admin to resend your invitation");
        }
        if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
            throw new InvalidArgumentException("Invitation has already been used");
        }
        if (invitation.getStatus() == InvitationStatus.EXPIRED || invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.expire();
            invitationRepository.save(invitation);
            throw new InvalidArgumentException("Invitation has expired");
        }
        return invitation;
    }

    public InvitationResponse toResponse(Invitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getToken(),
                invitation.getEmail(),
                invitation.getType(),
                invitation.getStatus(),
                invitation.getSection() == null ? null : invitation.getSection().getName(),
                invitation.getExpiresAt(),
                invitation.getMessage()
        );
    }
}
