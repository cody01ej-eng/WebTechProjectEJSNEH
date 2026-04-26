package com.tcu.projectpulse.project.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "peer_evaluation_criterion_score")
public class PeerEvaluationCriterionScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id")
    private PeerEvaluationItem item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criterion_id")
    private RubricCriterion criterion;

    @Column(nullable = false)
    private Integer score;

    protected PeerEvaluationCriterionScore() {
    }

    public PeerEvaluationCriterionScore(PeerEvaluationItem item, RubricCriterion criterion, Integer score) {
        this.item = item;
        this.criterion = criterion;
        this.score = score;
    }

    public PeerEvaluationItem getItem() {
        return item;
    }

    public RubricCriterion getCriterion() {
        return criterion;
    }

    public Integer getScore() {
        return score;
    }
}
