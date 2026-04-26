package com.tcu.projectpulse.user.domain;

import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.project.domain.SeniorDesignTeam;
import com.tcu.projectpulse.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_account")
public class UserAccount extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(length = 10)
    private String middleInitial;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private SeniorDesignSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id")
    private SeniorDesignTeam assignedTeam;

    protected UserAccount() {
    }

    public UserAccount(String firstName,
                       String middleInitial,
                       String lastName,
                       String email,
                       String passwordHash,
                       UserRole role,
                       UserStatus status,
                       SeniorDesignSection section) {
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.section = section;
    }

    public void updateProfile(String firstName, String middleInitial, String lastName, String email) {
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.lastName = lastName;
        this.email = email;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.DEACTIVATED;
    }

    public void assignToTeam(SeniorDesignTeam assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public void assignSection(SeniorDesignSection section) {
        this.section = section;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public SeniorDesignSection getSection() {
        return section;
    }

    public SeniorDesignTeam getAssignedTeam() {
        return assignedTeam;
    }
}
