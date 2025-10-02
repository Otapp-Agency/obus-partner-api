package com.obuspartners.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.obuspartners.modules.partner_integration.stations.StationServiceImpl;

/**
 * Test class to verify station caching functionality
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class StationServiceCacheTest {

    @InjectMocks
    private StationServiceImpl stationService;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        stationService.clearStationCache();
    }

    @Test
    void testCacheStatsWhenEmpty() {
        Map<String, Object> stats = stationService.getCacheStats();
        
        assertNotNull(stats);
        assertEquals(0, stats.get("cacheSize"));
        assertEquals(300L, stats.get("ttlSeconds")); // 5 minutes
        assertNull(stats.get("cachedAt"));
        assertEquals(true, stats.get("isExpired"));
    }

    @Test
    void testCacheClearing() {
        // Verify cache is initially empty
        Map<String, Object> stats = stationService.getCacheStats();
        assertEquals(0, stats.get("cacheSize"));
        
        // Clear cache (should not throw exception)
        assertDoesNotThrow(() -> stationService.clearStationCache());
        
        // Verify cache is still empty
        stats = stationService.getCacheStats();
        assertEquals(0, stats.get("cacheSize"));
    }

    @Test
    void testCacheTTLConfiguration() {
        Map<String, Object> stats = stationService.getCacheStats();
        assertEquals(300L, stats.get("ttlSeconds")); // 5 minutes = 300 seconds
    }
}
