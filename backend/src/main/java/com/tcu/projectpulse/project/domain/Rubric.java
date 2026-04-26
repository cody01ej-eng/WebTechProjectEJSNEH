package com.tcu.projectpulse.project.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rubric")
public class Rubric extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @OneToMany(mappedBy = "rubric", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RubricCriterion> criteria = new ArrayList<>();

    protected Rubric() {
    }

    public Rubric(String name) {
        this.name = name;
    }

    public void addCriterion(RubricCriterion criterion) {
        criteria.add(criterion);
    }

    public String getName() {
        return name;
    }

    public List<RubricCriterion> getCriteria() {
        return criteria;
    }
}
