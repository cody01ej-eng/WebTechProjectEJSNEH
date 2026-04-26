package com.tcu.projectpulse.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
