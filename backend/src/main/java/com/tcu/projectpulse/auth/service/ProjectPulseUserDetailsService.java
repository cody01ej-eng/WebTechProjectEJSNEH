package com.tcu.projectpulse.auth.service;

import com.tcu.projectpulse.auth.domain.AuthenticatedUser;
import com.tcu.projectpulse.user.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ProjectPulseUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public ProjectPulseUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAccountRepository.findByEmailIgnoreCase(username)
                .map(user -> new AuthenticatedUser(
                        user.getId(),
                        user.getEmail(),
                        user.getPasswordHash(),
                        user.getRole(),
                        user.getStatus()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Account not found for email " + username));
    }
}
