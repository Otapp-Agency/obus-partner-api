package com.obuspartners.modules.partner_integration.bmslg.agent_v8.repository;

import com.obuspartners.modules.partner_integration.bmslg.agent_v8.entity.BmsLgBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BmsLgBooking entity operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface BmsLgBookingRepository extends JpaRepository<BmsLgBooking, Long> {

    /**
     * Find BMSLG booking by UID
     * 
     * @param uid the unique identifier
     * @return Optional containing the booking if found
     */
    Optional<BmsLgBooking> findByUid(String uid);

    /**
     * Find BMSLG bookings by owner ID
     * 
     * @param ownerId the owner ID
     * @param pageable pagination information
     * @return Page of bookings
     */
    Page<BmsLgBooking> findByOwnerId(String ownerId, Pageable pageable);

    /**
     * Find BMSLG bookings by agent ID
     * 
     * @param agentId the agent ID
     * @param pageable pagination information
     * @return Page of bookings
     */
    Page<BmsLgBooking> findByAgentId(String agentId, Pageable pageable);

    /**
     * Find BMSLG bookings by owner ID and agent ID
     * 
     * @param ownerId the owner ID
     * @param agentId the agent ID
     * @param pageable pagination information
     * @return Page of bookings
     */
    Page<BmsLgBooking> findByOwnerIdAndAgentId(String ownerId, String agentId, Pageable pageable);

    /**
     * Find BMSLG bookings by phone number
     * 
     * @param phone the phone number
     * @param pageable pagination information
     * @return Page of bookings
     */
    Page<BmsLgBooking> findByPhone(String phone, Pageable pageable);

    /**
     * Find BMSLG bookings by email
     * 
     * @param email the email address
     * @param pageable pagination information
     * @return Page of bookings
     */
    Page<BmsLgBooking> findByEmail(String email, Pageable pageable);

    /**
     * Find BMSLG bookings by payment code
     * 
     * @param payCode the payment code
     * @param pageable pagination information
     * @return Page of bookings
     */
    Page<BmsLgBooking> findByPayCode(String payCode, Pageable pageable);

    /**
     * Find BMSLG bookings by currency
     * 
     * @param currency the currency code
     * @param pageable pagination information
     * @return Page of bookings
     */
    Page<BmsLgBooking> findByCurrency(String currency, Pageable pageable);

    /**
     * Count bookings by owner ID
     * 
     * @param ownerId the owner ID
     * @return count of bookings
     */
    long countByOwnerId(String ownerId);

    /**
     * Count bookings by agent ID
     * 
     * @param agentId the agent ID
     * @return count of bookings
     */
    long countByAgentId(String agentId);

    /**
     * Find bookings created within a date range
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return Page of bookings
     */
    @Query("SELECT b FROM BmsLgBooking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Page<BmsLgBooking> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                              @Param("endDate") java.time.LocalDateTime endDate, 
                                              Pageable pageable);

    /**
     * Find bookings by owner ID and date range
     * 
     * @param ownerId the owner ID
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return Page of bookings
     */
    @Query("SELECT b FROM BmsLgBooking b WHERE b.ownerId = :ownerId AND b.createdAt BETWEEN :startDate AND :endDate")
    Page<BmsLgBooking> findByOwnerIdAndCreatedAtBetween(@Param("ownerId") String ownerId,
                                                        @Param("startDate") java.time.LocalDateTime startDate,
                                                        @Param("endDate") java.time.LocalDateTime endDate,
                                                        Pageable pageable);

    /**
     * Check if booking exists by UID
     * 
     * @param uid the unique identifier
     * @return true if exists, false otherwise
     */
    boolean existsByUid(String uid);

    /**
     * Delete booking by UID
     * 
     * @param uid the unique identifier
     */
    void deleteByUid(String uid);
    
    // === BOOKING RELATIONSHIP QUERIES ===
    
    /**
     * Find BMSLG booking by main booking UID
     * 
     * @param bookingUid the main booking UID
     * @return Optional BmsLgBooking
     */
    Optional<BmsLgBooking> findByBookingUid(String bookingUid);
    
    /**
     * Find BMSLG bookings by main booking ID
     * 
     * @param bookingId the main booking ID
     * @return List of BmsLgBooking
     */
    List<BmsLgBooking> findByBookingId(Long bookingId);
    
    /**
     * Check if BMSLG booking exists for main booking UID
     * 
     * @param bookingUid the main booking UID
     * @return true if exists, false otherwise
     */
    boolean existsByBookingUid(String bookingUid);
}
