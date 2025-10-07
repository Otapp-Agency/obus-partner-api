package com.obuspartners.modules.partner_integration.bmslg.agent_v8.seat_map;

import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgSeatMapRequestDto;

public interface BmsLgSeatMapService {
    public Object getSeatMap(BmsLgSeatMapRequestDto requestDto);
}
