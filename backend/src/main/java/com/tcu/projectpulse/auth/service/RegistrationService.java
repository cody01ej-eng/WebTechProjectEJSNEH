package com.tcu.projectpulse.auth.service;

import com.tcu.projectpulse.auth.domain.Invitation;
import com.tcu.projectpulse.auth.domain.InvitationType;
import com.tcu.projectpulse.auth.dto.InstructorRegistrationRequest;
import com.tcu.projectpulse.auth.dto.StudentRegistrationRequest;
import com.tcu.projectpulse.shared.exception.ConflictException;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.dto.UserAccountResponse;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import com.tcu.projectpulse.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistrationService {

    private final InvitationService invitationService;
    private final UserAccountRepository userAccountRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(InvitationService invitationService,
                               UserAccountRepository userAccountRepository,
                               UserService userService,
                               PasswordEncoder passwordEncoder) {
        this.invitationService = invitationService;
        this.userAccountRepository = userAccountRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccountResponse registerStudent(StudentRegistrationRequest request) {
        Invitation invitation = invitationService.findPendingInvitation(request.token(), InvitationType.STUDENT);
        if (!invitation.getEmail().equalsIgnoreCase(request.email())) {
            throw new InvalidArgumentException("Student email must match the invitation email");
        }
        ensureEmailAvailable(request.email());
        UserAccount account = userAccountRepository.save(new UserAccount(
                request.firstName(),
                null,
                request.lastName(),
                request.email(),
                encodePassword(request.password()),
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                invitation.getSection()
        ));
        invitation.accept();
        return userService.getUser(account.getId());
    }

    public UserAccountResponse registerInstructor(InstructorRegistrationRequest request) {
        Invitation invitation = invitationService.findPendingInvitation(request.token(), InvitationType.INSTRUCTOR);
        if (!request.password().equals(request.confirmPassword())) {
            throw new InvalidArgumentException("Instructor password confirmation does not match");
        }
        ensureEmailAvailable(invitation.getEmail());
        UserAccount account = userAccountRepository.save(new UserAccount(
                request.firstName(),
                request.middleInitial(),
                request.lastName(),
                invitation.getEmail(),
                encodePassword(request.password()),
                UserRole.INSTRUCTOR,
                UserStatus.ACTIVE,
                null
        ));
        invitation.accept();
        return userService.getUser(account.getId());
    }

    private void ensureEmailAvailable(String email) {
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Account already exists for email " + email);
        }
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
