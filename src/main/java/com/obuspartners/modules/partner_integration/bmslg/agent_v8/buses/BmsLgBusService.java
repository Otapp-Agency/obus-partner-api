package com.obuspartners.modules.partner_integration.bmslg.agent_v8.buses;

import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgSearchBusRequestDto;

public interface BmsLgBusService {
    public Object searchBuses();
    public Object searchBuses(BmsLgSearchBusRequestDto requestDto);
}
