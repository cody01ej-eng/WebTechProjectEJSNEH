package com.tcu.projectpulse.auth.config;

import java.util.Properties;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfiguration {

    @Bean
    public JavaMailSender javaMailSender(MailProperties mailProperties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.getHost());
        if (mailProperties.getPort() != null) {
            mailSender.setPort(mailProperties.getPort());
        }
        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());
        if (mailProperties.getProtocol() != null) {
            mailSender.setProtocol(mailProperties.getProtocol());
        }
        if (mailProperties.getDefaultEncoding() != null) {
            mailSender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());
        }

        Properties javaMailProperties = new Properties();
        javaMailProperties.putAll(mailProperties.getProperties());
        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
    }
}
