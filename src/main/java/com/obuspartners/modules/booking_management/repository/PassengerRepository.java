package com.obuspartners.modules.booking_management.repository;

import com.obuspartners.modules.booking_management.domain.entity.Passenger;
import com.obuspartners.modules.booking_management.domain.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Passenger entity operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    /**
     * Find passenger by UID
     */
    Optional<Passenger> findByUid(String uid);

    /**
     * Find passengers by booking ID
     */
    List<Passenger> findByBookingId(Long bookingId);

    /**
     * Find passengers by booking UID
     */
    @Query("SELECT p FROM Passenger p WHERE p.booking.uid = :bookingUid")
    List<Passenger> findByBookingUid(@Param("bookingUid") String bookingUid);

    /**
     * Find passengers by ticket status
     */
    Page<Passenger> findByTicketStatus(TicketStatus ticketStatus, Pageable pageable);

    /**
     * Find passengers by booking and ticket status
     */
    List<Passenger> findByBookingIdAndTicketStatus(Long bookingId, TicketStatus ticketStatus);

    /**
     * Find passengers by seat ID
     */
    Optional<Passenger> findBySeatId(String seatId);

    /**
     * Find passengers by full name (case insensitive)
     */
    Page<Passenger> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    /**
     * Find passengers by phone number
     */
    List<Passenger> findByPhoneNumber(String phoneNumber);

    /**
     * Find passengers by email
     */
    List<Passenger> findByEmail(String email);

    /**
     * Find passengers by passport number
     */
    List<Passenger> findByPassportNumber(String passportNumber);

    /**
     * Find passengers by national ID
     */
    List<Passenger> findByNationalId(String nationalId);

    /**
     * Find passengers by external ticket ID
     */
    Optional<Passenger> findByExternalTicketId(String externalTicketId);

    /**
     * Count passengers by booking
     */
    long countByBookingId(Long bookingId);

    /**
     * Count passengers by ticket status
     */
    long countByTicketStatus(TicketStatus ticketStatus);

    /**
     * Check if passenger exists by UID
     */
    boolean existsByUid(String uid);

    /**
     * Check if seat is occupied
     */
    boolean existsBySeatId(String seatId);
}
