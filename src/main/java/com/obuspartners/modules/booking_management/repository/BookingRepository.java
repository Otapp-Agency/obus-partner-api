package com.obuspartners.modules.booking_management.repository;

import com.obuspartners.modules.booking_management.domain.entity.Booking;
import com.obuspartners.modules.booking_management.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for Booking entity operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find booking by UID
     */
    Optional<Booking> findByUid(String uid);

    /**
     * Find bookings by partner ID
     */
    Page<Booking> findByPartnerId(Long partnerId, Pageable pageable);

    /**
     * Find bookings by agent ID
     */
    Page<Booking> findByAgentId(Long agentId, Pageable pageable);

    /**
     * Find bookings by status
     */
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    /**
     * Find bookings by partner and status
     */
    Page<Booking> findByPartnerIdAndStatus(Long partnerId, BookingStatus status, Pageable pageable);

    /**
     * Find bookings by agent and status
     */
    Page<Booking> findByAgentIdAndStatus(Long agentId, BookingStatus status, Pageable pageable);

    /**
     * Find bookings by departure date range
     */
    @Query("SELECT b FROM Booking b WHERE b.departureDate BETWEEN :startDate AND :endDate")
    Page<Booking> findByDepartureDateBetween(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate, 
                                           Pageable pageable);

    /**
     * Find bookings by company name
     */
    Page<Booking> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);

    /**
     * Find bookings by route name
     */
    Page<Booking> findByRouteNameContainingIgnoreCase(String routeName, Pageable pageable);

    /**
     * Count bookings by status
     */
    long countByStatus(BookingStatus status);

    /**
     * Count bookings by partner
     */
    long countByPartnerId(Long partnerId);

    /**
     * Count bookings by agent
     */
    long countByAgentId(Long agentId);

    /**
     * Find bookings with external booking ID
     */
    Optional<Booking> findByExternalBookingId(String externalBookingId);

    /**
     * Check if booking exists by UID
     */
    boolean existsByUid(String uid);
}
