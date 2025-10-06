package com.obuspartners.modules.partner_integration.bmslg.agent_v8.repository;

import com.obuspartners.modules.partner_integration.bmslg.agent_v8.entity.BmsLgPassenger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BmsLgPassenger entity operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface BmsLgPassengerRepository extends JpaRepository<BmsLgPassenger, Long> {

    /**
     * Find BMSLG passenger by UID
     * 
     * @param uid the unique identifier
     * @return Optional containing the passenger if found
     */
    Optional<BmsLgPassenger> findByUid(String uid);

    /**
     * Find passengers by booking UID
     * 
     * @param bookingUid the booking UID
     * @return List of passengers
     */
    @Query("SELECT p FROM BmsLgPassenger p JOIN p.bmsLgBooking b WHERE b.uid = :bookingUid")
    List<BmsLgPassenger> findByBookingUid(@Param("bookingUid") String bookingUid);

    /**
     * Find passengers by booking ID
     * 
     * @param bookingId the booking ID
     * @return List of passengers
     */
    List<BmsLgPassenger> findByBmsLgBookingId(Long bookingId);

    /**
     * Find passengers by name
     * 
     * @param name the passenger name
     * @param pageable pagination information
     * @return Page of passengers
     */
    Page<BmsLgPassenger> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find passengers by seat ID
     * 
     * @param seatId the seat ID
     * @return List of passengers
     */
    List<BmsLgPassenger> findBySeatId(String seatId);

    /**
     * Find passengers by gender
     * 
     * @param gender the gender
     * @param pageable pagination information
     * @return Page of passengers
     */
    Page<BmsLgPassenger> findByGender(String gender, Pageable pageable);

    /**
     * Find passengers by category
     * 
     * @param category the passenger category
     * @param pageable pagination information
     * @return Page of passengers
     */
    Page<BmsLgPassenger> findByCategory(String category, Pageable pageable);

    /**
     * Find passengers by phone number
     * 
     * @param seatMob the phone number
     * @param pageable pagination information
     * @return Page of passengers
     */
    Page<BmsLgPassenger> findBySeatMob(String seatMob, Pageable pageable);

    /**
     * Find passengers by email
     * 
     * @param email the email address
     * @param pageable pagination information
     * @return Page of passengers
     */
    Page<BmsLgPassenger> findByEmail(String email, Pageable pageable);

    /**
     * Find passengers by passport number
     * 
     * @param passport the passport number
     * @param pageable pagination information
     * @return Page of passengers
     */
    Page<BmsLgPassenger> findByPassport(String passport, Pageable pageable);

    /**
     * Find passengers by boarding point
     * 
     * @param boarding the boarding point
     * @param pageable pagination information
     * @return Page of passengers
     */
    Page<BmsLgPassenger> findByBoarding(String boarding, Pageable pageable);

    /**
     * Find passengers by dropping point
     * 
     * @param dropping the dropping point
     * @param pageable pagination information
     * @return Page of passengers
     */
    Page<BmsLgPassenger> findByDropping(String dropping, Pageable pageable);

    /**
     * Find passengers by travel date
     * 
     * @param trvlDt the travel date
     * @param pageable pagination information
     * @return Page of passengers
     */
    Page<BmsLgPassenger> findByTrvlDt(String trvlDt, Pageable pageable);

    /**
     * Find passengers by from ID and to ID
     * 
     * @param fromId the from location ID
     * @param toId the to location ID
     * @param pageable pagination information
     * @return Page of passengers
     */
    Page<BmsLgPassenger> findByFromIdAndToId(String fromId, String toId, Pageable pageable);

    /**
     * Count passengers by booking ID
     * 
     * @param bookingId the booking ID
     * @return count of passengers
     */
    long countByBmsLgBookingId(Long bookingId);

    /**
     * Count passengers by gender
     * 
     * @param gender the gender
     * @return count of passengers
     */
    long countByGender(String gender);

    /**
     * Count passengers by category
     * 
     * @param category the passenger category
     * @return count of passengers
     */
    long countByCategory(String category);

    /**
     * Find passengers created within a date range
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return Page of passengers
     */
    @Query("SELECT p FROM BmsLgPassenger p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Page<BmsLgPassenger> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                                @Param("endDate") java.time.LocalDateTime endDate,
                                                Pageable pageable);

    /**
     * Find passengers by booking owner ID
     * 
     * @param ownerId the owner ID
     * @param pageable pagination information
     * @return Page of passengers
     */
    @Query("SELECT p FROM BmsLgPassenger p JOIN p.bmsLgBooking b WHERE b.ownerId = :ownerId")
    Page<BmsLgPassenger> findByBookingOwnerId(@Param("ownerId") String ownerId, Pageable pageable);

    /**
     * Find passengers by booking agent ID
     * 
     * @param agentId the agent ID
     * @param pageable pagination information
     * @return Page of passengers
     */
    @Query("SELECT p FROM BmsLgPassenger p JOIN p.bmsLgBooking b WHERE b.agentId = :agentId")
    Page<BmsLgPassenger> findByBookingAgentId(@Param("agentId") String agentId, Pageable pageable);

    /**
     * Check if passenger exists by UID
     * 
     * @param uid the unique identifier
     * @return true if exists, false otherwise
     */
    boolean existsByUid(String uid);

    /**
     * Check if seat is occupied for a specific travel date
     * 
     * @param seatId the seat ID
     * @param trvlDt the travel date
     * @return true if seat is occupied, false otherwise
     */
    boolean existsBySeatIdAndTrvlDt(String seatId, String trvlDt);

    /**
     * Delete passenger by UID
     * 
     * @param uid the unique identifier
     */
    void deleteByUid(String uid);

    /**
     * Delete all passengers by booking ID
     * 
     * @param bookingId the booking ID
     */
    void deleteByBmsLgBookingId(Long bookingId);
}
