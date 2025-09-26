package com.obuspartners.modules.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiting interceptor
 * In production, replace with Redis-based rate limiting
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    
    // Rate limit: 100 requests per minute per API key
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey == null) {
            // No API key, allow request (will be handled by authentication filter)
            return true;
        }

        long currentTime = System.currentTimeMillis();
        String key = apiKey + "_" + (currentTime / WINDOW_SIZE_MS);
        
        // Clean up old entries periodically
        if (requestCounts.size() > 1000) {
            cleanupOldEntries(currentTime);
        }
        
        AtomicInteger count = requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();
        
        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for API key: {} ({} requests)", apiKey, currentCount);
            response.setStatus(429); // Too Many Requests
            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf((currentTime / WINDOW_SIZE_MS + 1) * WINDOW_SIZE_MS));
            return false;
        }
        
        // Set rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(MAX_REQUESTS_PER_MINUTE - currentCount));
        response.setHeader("X-RateLimit-Reset", String.valueOf((currentTime / WINDOW_SIZE_MS + 1) * WINDOW_SIZE_MS));
        
        return true;
    }
    
    private void cleanupOldEntries(long currentTime) {
        long cutoffTime = currentTime - (WINDOW_SIZE_MS * 2); // Keep 2 windows
        requestCounts.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            String[] parts = key.split("_");
            if (parts.length >= 2) {
                try {
                    long windowTime = Long.parseLong(parts[parts.length - 1]) * WINDOW_SIZE_MS;
                    return windowTime < cutoffTime;
                } catch (NumberFormatException e) {
                    return true; // Remove malformed entries
                }
            }
            return true;
        });
    }
}
