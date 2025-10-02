package com.obuspartners.modules.partner_integration.stations;

import java.util.Map;

public interface StationService {

    public Object fetchAllStations();
    
    /**
     * Clear the station cache manually
     */
    public void clearStationCache();
    
    /**
     * Get cache statistics for monitoring
     * @return Map containing cache statistics
     */
    public Map<String, Object> getCacheStats();

}
