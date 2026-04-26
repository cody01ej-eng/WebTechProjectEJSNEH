package com.tcu.projectpulse.project.domain;

import com.tcu.projectpulse.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "senior_design_team")
public class SeniorDesignTeam extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id")
    private SeniorDesignSection section;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(length = 255)
    private String websiteUrl;

    protected SeniorDesignTeam() {
    }

    public SeniorDesignTeam(SeniorDesignSection section, String name, String description, String websiteUrl) {
        this.section = section;
        this.name = name;
        this.description = description;
        this.websiteUrl = websiteUrl;
    }

    public void update(String name, String description, String websiteUrl) {
        this.name = name;
        this.description = description;
        this.websiteUrl = websiteUrl;
    }

    public SeniorDesignSection getSection() {
        return section;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }
}
