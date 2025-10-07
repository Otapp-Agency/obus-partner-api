package com.obuspartners.modules.partner_integration.bmslg.agent_v8.stations;

import java.util.Map;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgStationRequestDto;

public interface BmsLgStationService {

    // public Object fetchAllStations();
    
    public Object fetchAllStations(BmsLgStationRequestDto requestDto);
    
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
