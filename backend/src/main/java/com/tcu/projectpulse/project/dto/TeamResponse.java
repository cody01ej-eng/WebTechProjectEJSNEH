package com.tcu.projectpulse.project.dto;

import java.util.List;

public record TeamResponse(
        Long id,
        String sectionName,
        String name,
        String description,
        String websiteUrl,
        List<MemberView> students,
        List<MemberView> instructors
) {
    public record MemberView(Long id, String name, String email) {
    }
}
