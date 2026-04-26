package com.tcu.projectpulse.project.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import com.tcu.projectpulse.user.domain.UserAccount;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_instructor_assignment")
public class TeamInstructorAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private SeniorDesignTeam team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id")
    private UserAccount instructor;

    protected TeamInstructorAssignment() {
    }

    public TeamInstructorAssignment(SeniorDesignTeam team, UserAccount instructor) {
        this.team = team;
        this.instructor = instructor;
    }

    public SeniorDesignTeam getTeam() {
        return team;
    }

    public UserAccount getInstructor() {
        return instructor;
    }
}
