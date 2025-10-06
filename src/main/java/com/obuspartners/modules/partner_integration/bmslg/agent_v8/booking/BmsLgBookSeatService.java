package com.obuspartners.modules.partner_integration.bmslg.agent_v8.booking;

import com.obuspartners.modules.booking_management.domain.dto.BookingResponseDto;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgBookSeatRequestDto;

public interface BmsLgBookSeatService {

    BookingResponseDto createBookingRequest(BmsLgBookSeatRequestDto bookingRequest);


    Object bookSeat(BmsLgBookSeatRequestDto bookingRequest);
}
