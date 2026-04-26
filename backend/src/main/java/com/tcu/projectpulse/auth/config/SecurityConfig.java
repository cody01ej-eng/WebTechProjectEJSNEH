package com.tcu.projectpulse.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcu.projectpulse.shared.api.Result;
import com.tcu.projectpulse.shared.api.StatusCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final boolean requireHttps;
    private final List<String> allowedOrigins;

    public SecurityConfig(ObjectMapper objectMapper,
                          @Value("${app.security.require-https:true}") boolean requireHttps,
                          @Value("${app.security.allowed-origins:}") List<String> allowedOrigins) {
        this.objectMapper = objectMapper;
        this.requireHttps = requireHttps;
        this.allowedOrigins = allowedOrigins.stream().map(String::trim).filter(origin -> !origin.isBlank()).toList();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (requireHttps) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http.csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository()))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registrations/students").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/registrations/instructors").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeFailure(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        Result.failure(StatusCode.UNAUTHORIZED, "Authentication required")))
                        .accessDeniedHandler((request, response, exception) -> {
                            Result<Void> result = exception instanceof CsrfException
                                    ? Result.failure(StatusCode.FORBIDDEN, "CSRF token missing or invalid")
                                    : Result.failure(StatusCode.FORBIDDEN, "Access denied");
                            writeFailure(response, HttpServletResponse.SC_FORBIDDEN, result);
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        if (allowedOrigins.isEmpty()) {
            return request -> null;
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Accept",
                "Content-Type",
                "X-Requested-With",
                "X-CSRF-TOKEN",
                "X-XSRF-TOKEN"
        ));
        configuration.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-CSRF-TOKEN");
        return repository;
    }

    private void writeFailure(HttpServletResponse response, int status, Result<Void> result) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), result);
    }
}
