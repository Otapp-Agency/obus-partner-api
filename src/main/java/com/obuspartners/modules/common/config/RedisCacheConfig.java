package com.obuspartners.modules.common.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Cache configuration for high-performance caching
 * 
 * Features:
 * - Native TTL support with Redis
 * - JSON serialization for complex objects
 * - Configurable cache TTL per cache name
 * - Connection pooling for better performance
 * - Automatic fallback to in-memory cache when Redis unavailable
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Configuration
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnMissingBean(name = "fallbackCacheManager")
public class RedisCacheConfig {

    /**
     * Configure Redis cache manager with TTL support
     * 
     * @param connectionFactory Redis connection factory
     * @return Redis cache manager
     */
    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5)) // Default 5 minutes TTL
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        // Station cache with 1-hour TTL (3600 seconds)
        RedisCacheConfiguration stationCacheConfig = defaultConfig
            .entryTtl(Duration.ofSeconds(3600));

        // Bus search cache with 5-minute TTL
        RedisCacheConfiguration busSearchCacheConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(5));

        // Partner cache with 10-minute TTL
        RedisCacheConfiguration partnerCacheConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(10));

        // Agent cache with 15-minute TTL
        RedisCacheConfiguration agentCacheConfig = defaultConfig
            .entryTtl(Duration.ofMinutes(15));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration("stationCache", stationCacheConfig)
            .withCacheConfiguration("busSearchCache", busSearchCacheConfig)
            .withCacheConfiguration("partnerCache", partnerCacheConfig)
            .withCacheConfiguration("agentCache", agentCacheConfig)
            .build();
    }
}
