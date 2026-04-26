package com.tcu.projectpulse.auth.domain;

import com.tcu.projectpulse.project.domain.SeniorDesignSection;
import com.tcu.projectpulse.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitation")
public class Invitation extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private SeniorDesignSection section;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(length = 1000)
    private String message;

    protected Invitation() {
    }

    public Invitation(String token,
                      String email,
                      InvitationType type,
                      InvitationStatus status,
                      SeniorDesignSection section,
                      LocalDateTime expiresAt,
                      String message) {
        this.token = token;
        this.email = email;
        this.type = type;
        this.status = status;
        this.section = section;
        this.expiresAt = expiresAt;
        this.message = message;
    }

    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
    }

    public void fail() {
        this.status = InvitationStatus.FAILED;
    }

    public void expire() {
        this.status = InvitationStatus.EXPIRED;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public InvitationType getType() {
        return type;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public SeniorDesignSection getSection() {
        return section;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public String getMessage() {
        return message;
    }
}
