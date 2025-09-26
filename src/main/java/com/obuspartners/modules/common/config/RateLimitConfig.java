package com.obuspartners.modules.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Rate limiting configuration for API endpoints
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    /**
     * Configure rate limiting for API endpoints
     * In production, integrate with Redis-based rate limiting
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Add rate limiting interceptor for API key validation endpoints
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/partner/**")
                .excludePathPatterns("/api/partner/health", "/api/partner/docs");
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor();
    }
}
