package com.tcu.projectpulse.project.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import com.tcu.projectpulse.user.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "weekly_activity")
public class WeeklyActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private UserAccount student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private SeniorDesignTeam team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "active_week_id")
    private ActiveWeek activeWeek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityCategory category;

    @Column(nullable = false, length = 255)
    private String plannedActivity;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal plannedHours;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal actualHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityProgressStatus status;

    protected WeeklyActivity() {
    }

    public WeeklyActivity(UserAccount student,
                          SeniorDesignTeam team,
                          ActiveWeek activeWeek,
                          ActivityCategory category,
                          String plannedActivity,
                          String description,
                          BigDecimal plannedHours,
                          BigDecimal actualHours,
                          ActivityProgressStatus status) {
        this.student = student;
        this.team = team;
        this.activeWeek = activeWeek;
        this.category = category;
        this.plannedActivity = plannedActivity;
        this.description = description;
        this.plannedHours = plannedHours;
        this.actualHours = actualHours;
        this.status = status;
    }

    public void update(ActivityCategory category,
                       String plannedActivity,
                       String description,
                       BigDecimal plannedHours,
                       BigDecimal actualHours,
                       ActivityProgressStatus status) {
        this.category = category;
        this.plannedActivity = plannedActivity;
        this.description = description;
        this.plannedHours = plannedHours;
        this.actualHours = actualHours;
        this.status = status;
    }

    public UserAccount getStudent() {
        return student;
    }

    public SeniorDesignTeam getTeam() {
        return team;
    }

    public ActiveWeek getActiveWeek() {
        return activeWeek;
    }

    public ActivityCategory getCategory() {
        return category;
    }

    public String getPlannedActivity() {
        return plannedActivity;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPlannedHours() {
        return plannedHours;
    }

    public BigDecimal getActualHours() {
        return actualHours;
    }

    public ActivityProgressStatus getStatus() {
        return status;
    }
}
