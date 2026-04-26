package com.tcu.projectpulse.auth.repository;

import com.tcu.projectpulse.auth.domain.Invitation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByToken(String token);
}
