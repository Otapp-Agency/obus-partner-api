package com.obuspartners.modules.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for API key validation and other caching needs
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager for API key validation
     * In production, replace with Redis cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Configure cache names
        cacheManager.setCacheNames(java.util.Arrays.asList("partnerCache", "agentCache"));
        
        // Allow dynamic cache creation and null values
        cacheManager.setAllowNullValues(true);
        
        return cacheManager;
    }
}
