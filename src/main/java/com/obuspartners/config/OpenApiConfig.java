package com.obuspartners.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI/Swagger configuration for OBUS Partner API
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Global OpenAPI configuration with JWT Bearer Token and API Key security schemes.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth")
                        .addList("apiKey")
                        .addList("apiSecret"))
                .components(new Components()
                        // JWT Bearer Token for regular users
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token for authenticated users"))
                        // API Key header for partner authentication
                        .addSecuritySchemes("apiKey",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("Partner API Key"))
                        // API Secret header for partner authentication
                        .addSecuritySchemes("apiSecret",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Secret")
                                        .description("Partner API Secret")))
                .info(new Info()
                        .title("OBUS Partner API")
                        .version("1.0.0")
                        .description("Partner integration and management API for OBUS platform")
                        .contact(new Contact()
                                .name("OBUS Team")
                                .email("support@otapp.live")));
    }

    /**
     * All APIs group - shows everything
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/**")
                .build();
    }

    /**
     * Admin API group
     * Includes all admin endpoints for managing partners, users, agents, and bus systems.
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch(
                        "/admin/v1/**",
                        "/v1/auth/login",
                        "/v1/auth/refresh",
                        "/v1/auth/password/**")
                .build();
    }

    /**
     * Auth API group
     * Includes authentication endpoints (login, register, refresh, password reset).
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch(
                        "/v1/auth/**")
                .build();
    }

    /**
     * Partner API group
     * Includes partner integration endpoints for bus operations (stations, buses, bookings).
     */
    @Bean
    public GroupedOpenApi partnerApi() {
        return GroupedOpenApi.builder()
                .group("partner")
                .pathsToMatch(
                        "/partner/v1/**",
                        "/v1/auth/agent/**")
                .build();
    }

    /**
     * Public API group
     * Includes public endpoints like payment callbacks and webhooks (no authentication required).
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch(
                        "/public/**")
                .build();
    }
}
