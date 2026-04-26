package com.tcu.projectpulse.user.repository;

import com.tcu.projectpulse.user.domain.UserAccount;
import com.tcu.projectpulse.user.domain.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRole(UserRole role);

    List<UserAccount> findByRole(UserRole role);

    List<UserAccount> findByAssignedTeamId(Long teamId);

    List<UserAccount> findBySectionIdAndRoleOrderByLastNameAscFirstNameAsc(Long sectionId, UserRole role);
}
