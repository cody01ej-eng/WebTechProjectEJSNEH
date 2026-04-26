package com.tcu.projectpulse.project.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import com.tcu.projectpulse.user.domain.UserAccount;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "peer_evaluation_item")
public class PeerEvaluationItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id")
    private PeerEvaluationSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluatee_id")
    private UserAccount evaluatee;

    @Column(length = 1000)
    private String publicComment;

    @Column(length = 1000)
    private String privateComment;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PeerEvaluationCriterionScore> criterionScores = new ArrayList<>();

    protected PeerEvaluationItem() {
    }

    public PeerEvaluationItem(PeerEvaluationSubmission submission,
                              UserAccount evaluatee,
                              String publicComment,
                              String privateComment) {
        this.submission = submission;
        this.evaluatee = evaluatee;
        this.publicComment = publicComment;
        this.privateComment = privateComment;
    }

    public void addCriterionScore(PeerEvaluationCriterionScore score) {
        criterionScores.add(score);
    }

    public PeerEvaluationSubmission getSubmission() {
        return submission;
    }

    public UserAccount getEvaluatee() {
        return evaluatee;
    }

    public String getPublicComment() {
        return publicComment;
    }

    public String getPrivateComment() {
        return privateComment;
    }

    public List<PeerEvaluationCriterionScore> getCriterionScores() {
        return criterionScores;
    }
}
