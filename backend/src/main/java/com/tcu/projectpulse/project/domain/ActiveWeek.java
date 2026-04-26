package com.tcu.projectpulse.project.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "active_week")
public class ActiveWeek extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id")
    private SeniorDesignSection section;

    @Column(nullable = false)
    private LocalDate weekStartDate;

    @Column(nullable = false)
    private boolean active;

    protected ActiveWeek() {
    }

    public ActiveWeek(SeniorDesignSection section, LocalDate weekStartDate, boolean active) {
        this.section = section;
        this.weekStartDate = weekStartDate;
        this.active = active;
    }

    public SeniorDesignSection getSection() {
        return section;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public boolean isActive() {
        return active;
    }
}
