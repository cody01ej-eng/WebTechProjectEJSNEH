package com.tcu.projectpulse.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.email")
public record InvitationEmailProperties(
        boolean enabled,
        String baseUrl,
        String fromAddress,
        String fromName
) {

    public InvitationEmailProperties {
        baseUrl = normalize(baseUrl);
        fromAddress = normalize(fromAddress);
        fromName = normalize(fromName);
    }

    public String resolvedFromName() {
        return StringUtils.hasText(fromName) ? fromName : "Project Pulse Team";
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
