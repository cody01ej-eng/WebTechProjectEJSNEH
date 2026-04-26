package com.tcu.projectpulse.project.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "rubric_criterion")
public class RubricCriterion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rubric_id")
    private Rubric rubric;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal maxScore;

    @Column(nullable = false)
    private Integer displayOrder;

    protected RubricCriterion() {
    }

    public RubricCriterion(Rubric rubric, String name, String description, BigDecimal maxScore, Integer displayOrder) {
        this.rubric = rubric;
        this.name = name;
        this.description = description;
        this.maxScore = maxScore;
        this.displayOrder = displayOrder;
    }

    public Rubric getRubric() {
        return rubric;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
