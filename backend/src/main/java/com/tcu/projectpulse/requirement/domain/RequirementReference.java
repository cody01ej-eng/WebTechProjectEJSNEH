package com.tcu.projectpulse.requirement.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "requirement_reference")
public class RequirementReference extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String referenceKey;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequirementSource source;

    @Column(nullable = false, length = 1000)
    private String summary;

    protected RequirementReference() {
    }

    public RequirementReference(String referenceKey, String title, RequirementSource source, String summary) {
        this.referenceKey = referenceKey;
        this.title = title;
        this.source = source;
        this.summary = summary;
    }

    public String getReferenceKey() {
        return referenceKey;
    }

    public String getTitle() {
        return title;
    }

    public RequirementSource getSource() {
        return source;
    }

    public String getSummary() {
        return summary;
    }
}
