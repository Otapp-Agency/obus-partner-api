package com.obuspartners.modules.booking_management.service;

import com.obuspartners.modules.booking_management.domain.dto.BookingResponseDto;
import com.obuspartners.modules.booking_management.domain.dto.CreateBookingRequestDto;
import com.obuspartners.modules.booking_management.domain.entity.Booking;
import com.obuspartners.modules.booking_management.domain.entity.Passenger;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgBookSeatRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for booking management operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface BookingService {

    /**
     * Create a new booking
     * 
     * @param request the booking creation request
     * @return booking response with UID and status
     */
    BookingResponseDto createBooking(CreateBookingRequestDto request, BmsLgBookSeatRequestDto bmsLgRequest);

    /**
     * Get booking by UID
     * 
     * @param bookingUid the booking UID
     * @return booking details
     */
    Booking getBookingByUid(String bookingUid);

    /**
     * Get all bookings with pagination
     * 
     * @param pageable pagination parameters
     * @return page of bookings
     */
    Page<Booking> getAllBookings(Pageable pageable);

    /**
     * Get bookings by partner ID
     * 
     * @param partnerId the partner ID
     * @param pageable pagination parameters
     * @return page of bookings
     */
    Page<Booking> getBookingsByPartner(Long partnerId, Pageable pageable);

    /**
     * Get bookings by agent ID
     * 
     * @param agentId the agent ID
     * @param pageable pagination parameters
     * @return page of bookings
     */
    Page<Booking> getBookingsByAgent(Long agentId, Pageable pageable);

    /**
     * Get passengers for a booking
     * 
     * @param bookingUid the booking UID
     * @return list of passengers
     */
    List<Passenger> getPassengersByBookingUid(String bookingUid);

    /**
     * Get passenger by UID
     * 
     * @param passengerUid the passenger UID
     * @return passenger details
     */
    Passenger getPassengerByUid(String passengerUid);

    /**
     * Cancel a passenger ticket
     * 
     * @param passengerUid the passenger UID
     * @param reason cancellation reason
     * @param cancellationType type of cancellation
     * @return updated passenger details
     */
    Passenger cancelPassengerTicket(String passengerUid, String reason, String cancellationType);

    /**
     * Process refund for a passenger
     * 
     * @param passengerUid the passenger UID
     * @param refundAmount refund amount
     * @param refundReference refund reference
     * @return updated passenger details
     */
    Passenger processPassengerRefund(String passengerUid, java.math.BigDecimal refundAmount, String refundReference);
}
