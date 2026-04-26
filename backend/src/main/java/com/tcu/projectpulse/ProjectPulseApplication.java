package com.tcu.projectpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProjectPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectPulseApplication.class, args);
    }
}
