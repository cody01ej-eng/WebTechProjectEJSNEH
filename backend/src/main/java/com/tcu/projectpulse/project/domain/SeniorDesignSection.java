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
@Table(name = "senior_design_section")
public class SeniorDesignSection extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rubric_id")
    private Rubric rubric;

    protected SeniorDesignSection() {
    }

    public SeniorDesignSection(String name, LocalDate startDate, LocalDate endDate, Rubric rubric) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rubric = rubric;
    }

    public void update(String name, LocalDate startDate, LocalDate endDate, Rubric rubric) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rubric = rubric;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Rubric getRubric() {
        return rubric;
    }
}
