package com.obuspartners.modules.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Fallback cache configuration for when Redis is not available
 * 
 * This configuration will only be used when Redis is not available or
 * RedisConnectionFactory is not present in the classpath.
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Configuration
@EnableCaching
@ConditionalOnMissingBean(RedisConnectionFactory.class)
public class CacheConfig {

    /**
     * Configure fallback cache manager when Redis is not available
     * This will be used automatically when Redis is not running
     */
    @Bean
    public CacheManager fallbackCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Configure cache names including station cache
        cacheManager.setCacheNames(java.util.Arrays.asList("partnerCache", "agentCache", "stationCache"));
        
        // Allow dynamic cache creation and null values
        cacheManager.setAllowNullValues(true);
        
        return cacheManager;
    }
}
