package com.obuspartners.modules.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
     * Configure cache manager for API key validation and station data
     * In production, replace with Redis cache manager for better performance and TTL support
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Configure cache names including station cache
        cacheManager.setCacheNames(java.util.Arrays.asList("partnerCache", "agentCache", "stationCache"));
        
        // Allow dynamic cache creation and null values
        cacheManager.setAllowNullValues(true);
        
        return cacheManager;
    }
}
