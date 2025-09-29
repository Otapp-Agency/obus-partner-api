package com.obuspartners.modules.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for OBUS Partner API
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final AgentJwtRequestFilter agentJwtRequestFilter;

    @Autowired
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         JwtRequestFilter jwtRequestFilter,
                         AgentJwtRequestFilter agentJwtRequestFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
        this.agentJwtRequestFilter = agentJwtRequestFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/v1/auth/login", "/v1/auth/register", "/v1/auth/refresh", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/v1/auth/agent/login").permitAll()
                .requestMatchers("/v1/partners/public/**").permitAll()
                .requestMatchers("/demo/kafka/**", "/demo/email/**").permitAll() // Allow demo Kafka and email endpoints without authentication
                .requestMatchers("/partner/**").authenticated() // Just require authentication, no specific role
                .requestMatchers("/api/agent/**").hasRole("AGENT")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(agentJwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
