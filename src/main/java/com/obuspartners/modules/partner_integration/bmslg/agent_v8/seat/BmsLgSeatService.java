package com.obuspartners.modules.partner_integration.bmslg.agent_v8.seat;

import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgProcessSeatRequestDto;

public interface BmsLgSeatService {
    public Object processSeat(BmsLgProcessSeatRequestDto requestDto);
}
