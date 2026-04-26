package com.tcu.projectpulse.requirement.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "traceability_link")
public class TraceabilityLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requirement_id")
    private RequirementReference requirement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComponentType componentType;

    @Column(nullable = false, length = 255)
    private String componentName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TraceabilityStatus status;

    @Column(nullable = false, length = 1000)
    private String notes;

    protected TraceabilityLink() {
    }

    public TraceabilityLink(RequirementReference requirement,
                            ComponentType componentType,
                            String componentName,
                            TraceabilityStatus status,
                            String notes) {
        this.requirement = requirement;
        this.componentType = componentType;
        this.componentName = componentName;
        this.status = status;
        this.notes = notes;
    }

    public RequirementReference getRequirement() {
        return requirement;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public String getComponentName() {
        return componentName;
    }

    public TraceabilityStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }
}
