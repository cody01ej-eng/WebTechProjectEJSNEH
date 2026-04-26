package com.tcu.projectpulse.auth.service;

import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.domain.UserStatus;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBootstrapService implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final boolean enabled;
    private final boolean allowNonLocal;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;

    public AdminBootstrapService(UserAccountRepository userAccountRepository,
                                 PasswordEncoder passwordEncoder,
                                 Environment environment,
                                 @Value("${app.auth.bootstrap-admin.enabled:true}") boolean enabled,
                                 @Value("${app.auth.bootstrap-admin.allow-nonlocal:false}") boolean allowNonLocal,
                                 @Value("${app.auth.bootstrap-admin.email:admin@projectpulse.local}") String email,
                                 @Value("${app.auth.bootstrap-admin.password:Admin123!}") String password,
                                 @Value("${app.auth.bootstrap-admin.first-name:Project}") String firstName,
                                @Value("${app.auth.bootstrap-admin.last-name:Pulse Admin}") String lastName) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.enabled = enabled;
        this.allowNonLocal = allowNonLocal;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    @Transactional
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        if (!allowNonLocal && !isLocalProfileActive()) {
            throw new IllegalStateException(
                    "Bootstrap admin is only allowed for local/dev profiles unless "
                            + "app.auth.bootstrap-admin.allow-nonlocal=true is set explicitly");
        }
        if (userAccountRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        userAccountRepository.save(new UserAccount(
                firstName,
                null,
                lastName,
                email,
                passwordEncoder.encode(password),
                UserRole.ADMIN,
                UserStatus.ACTIVE,
                null
        ));
    }

    private boolean isLocalProfileActive() {
        for (String profile : environment.getActiveProfiles()) {
            if ("local".equalsIgnoreCase(profile) || "dev".equalsIgnoreCase(profile) || "test".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
