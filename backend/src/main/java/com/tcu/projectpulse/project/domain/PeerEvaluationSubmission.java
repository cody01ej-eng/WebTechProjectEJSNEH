package com.tcu.projectpulse.project.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import com.tcu.projectpulse.user.domain.UserAccount;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "peer_evaluation_submission")
public class PeerEvaluationSubmission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private UserAccount author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private SeniorDesignTeam team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "active_week_id")
    private ActiveWeek activeWeek;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PeerEvaluationItem> items = new ArrayList<>();

    protected PeerEvaluationSubmission() {
    }

    public PeerEvaluationSubmission(UserAccount author, SeniorDesignTeam team, ActiveWeek activeWeek) {
        this.author = author;
        this.team = team;
        this.activeWeek = activeWeek;
    }

    public void addItem(PeerEvaluationItem item) {
        items.add(item);
    }

    public UserAccount getAuthor() {
        return author;
    }

    public SeniorDesignTeam getTeam() {
        return team;
    }

    public ActiveWeek getActiveWeek() {
        return activeWeek;
    }

    public List<PeerEvaluationItem> getItems() {
        return items;
    }
}
