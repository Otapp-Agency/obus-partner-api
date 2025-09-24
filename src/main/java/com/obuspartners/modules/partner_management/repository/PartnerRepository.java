package com.obuspartners.modules.partner_management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.domain.enums.PartnerStatus;
import com.obuspartners.modules.partner_management.domain.enums.PartnerTier;
import com.obuspartners.modules.partner_management.domain.enums.PartnerType;

import java.util.Optional;

/**
 * Repository interface for Partner entity
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    /**
     * Find partner by unique UID
     * 
     * @param uid the partner UID
     * @return Optional containing the partner if found
     */
    Optional<Partner> findByUid(String uid);

    /**
     * Find partner by unique partner code
     * 
     * @param partnerCode the partner code
     * @return Optional containing the partner if found
     */
    Optional<Partner> findByPartnerCode(String partnerCode);

    /**
     * Find partner by email address
     * 
     * @param email the email address
     * @return Optional containing the partner if found
     */
    Optional<Partner> findByEmail(String email);

    /**
     * Find partner by phone number
     * 
     * @param phoneNumber the phone number
     * @return Optional containing the partner if found
     */
    Optional<Partner> findByPhoneNumber(String phoneNumber);

    /**
     * Find partner by business registration number
     * 
     * @param businessRegistrationNumber the business registration number
     * @return Optional containing the partner if found
     */
    Optional<Partner> findByBusinessRegistrationNumber(String businessRegistrationNumber);

    /**
     * Find partner by tax identification number
     * 
     * @param taxIdentificationNumber the tax identification number
     * @return Optional containing the partner if found
     */
    Optional<Partner> findByTaxIdentificationNumber(String taxIdentificationNumber);

    /**
     * Check if UID exists
     * 
     * @param uid the partner UID
     * @return true if UID exists, false otherwise
     */
    boolean existsByUid(String uid);

    /**
     * Check if partner code exists
     * 
     * @param partnerCode the partner code
     * @return true if partner code exists, false otherwise
     */
    boolean existsByPartnerCode(String partnerCode);

    /**
     * Check if email exists
     * 
     * @param email the email address
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number exists
     * 
     * @param phoneNumber the phone number
     * @return true if phone number exists, false otherwise
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Check if business registration number exists
     * 
     * @param businessRegistrationNumber the business registration number
     * @return true if business registration number exists, false otherwise
     */
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);

    /**
     * Check if tax identification number exists
     * 
     * @param taxIdentificationNumber the tax identification number
     * @return true if tax identification number exists, false otherwise
     */
    boolean existsByTaxIdentificationNumber(String taxIdentificationNumber);

    /**
     * Find partners by status
     * 
     * @param status the partner status
     * @param pageable pagination information
     * @return Page of partners with the specified status
     */
    Page<Partner> findByStatus(PartnerStatus status, Pageable pageable);

    /**
     * Find partners by type
     * 
     * @param type the partner type
     * @param pageable pagination information
     * @return Page of partners with the specified type
     */
    Page<Partner> findByType(PartnerType type, Pageable pageable);

    /**
     * Find partners by tier
     * 
     * @param tier the partner tier
     * @param pageable pagination information
     * @return Page of partners with the specified tier
     */
    Page<Partner> findByTier(PartnerTier tier, Pageable pageable);

    /**
     * Find active partners
     * 
     * @param pageable pagination information
     * @return Page of active partners
     */
    Page<Partner> findByIsActiveTrue(Pageable pageable);

    /**
     * Find verified partners
     * 
     * @param pageable pagination information
     * @return Page of verified partners
     */
    Page<Partner> findByIsVerifiedTrue(Pageable pageable);

    /**
     * Find partners by city
     * 
     * @param city the city name
     * @param pageable pagination information
     * @return Page of partners in the specified city
     */
    Page<Partner> findByCity(String city, Pageable pageable);

    /**
     * Find partners by state
     * 
     * @param state the state name
     * @param pageable pagination information
     * @return Page of partners in the specified state
     */
    Page<Partner> findByState(String state, Pageable pageable);

    /**
     * Find partners by country
     * 
     * @param country the country name
     * @param pageable pagination information
     * @return Page of partners in the specified country
     */
    Page<Partner> findByCountry(String country, Pageable pageable);

    /**
     * Search partners by business name containing the given text
     * 
     * @param businessName the business name to search for
     * @param pageable pagination information
     * @return Page of partners with business names containing the search text
     */
    Page<Partner> findByBusinessNameContainingIgnoreCase(String businessName, Pageable pageable);

    /**
     * Search partners by legal name containing the given text
     * 
     * @param legalName the legal name to search for
     * @param pageable pagination information
     * @return Page of partners with legal names containing the search text
     */
    Page<Partner> findByLegalNameContainingIgnoreCase(String legalName, Pageable pageable);

    /**
     * Find partners by status and type
     * 
     * @param status the partner status
     * @param type the partner type
     * @param pageable pagination information
     * @return Page of partners matching both criteria
     */
    Page<Partner> findByStatusAndType(PartnerStatus status, PartnerType type, Pageable pageable);

    /**
     * Find partners by status and tier
     * 
     * @param status the partner status
     * @param tier the partner tier
     * @param pageable pagination information
     * @return Page of partners matching both criteria
     */
    Page<Partner> findByStatusAndTier(PartnerStatus status, PartnerTier tier, Pageable pageable);

    /**
     * Find partners by type and tier
     * 
     * @param type the partner type
     * @param tier the partner tier
     * @param pageable pagination information
     * @return Page of partners matching both criteria
     */
    Page<Partner> findByTypeAndTier(PartnerType type, PartnerTier tier, Pageable pageable);

    /**
     * Count partners by status
     * 
     * @param status the partner status
     * @return number of partners with the specified status
     */
    long countByStatus(PartnerStatus status);

    /**
     * Count partners by type
     * 
     * @param type the partner type
     * @return number of partners with the specified type
     */
    long countByType(PartnerType type);

    /**
     * Count partners by tier
     * 
     * @param tier the partner tier
     * @return number of partners with the specified tier
     */
    long countByTier(PartnerTier tier);

    /**
     * Count active partners
     * 
     * @return number of active partners
     */
    long countByIsActiveTrue();

    /**
     * Count verified partners
     * 
     * @return number of verified partners
     */
    long countByIsVerifiedTrue();

    /**
     * Find partners with commission rate greater than specified value
     * 
     * @param commissionRate the minimum commission rate
     * @param pageable pagination information
     * @return Page of partners with commission rate greater than specified value
     */
    Page<Partner> findByCommissionRateGreaterThan(Double commissionRate, Pageable pageable);


    /**
     * Custom query to search partners by multiple criteria
     * 
     * @param businessName business name search term (optional)
     * @param city city filter (optional)
     * @param status status filter (optional)
     * @param type type filter (optional)
     * @param pageable pagination information
     * @return Page of partners matching the search criteria
     */
    @Query("SELECT p FROM Partner p WHERE " +
           "(:businessName IS NULL OR LOWER(p.businessName) LIKE LOWER(CONCAT('%', :businessName, '%'))) AND " +
           "(:city IS NULL OR LOWER(p.city) = LOWER(:city)) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:type IS NULL OR p.type = :type)")
    Page<Partner> searchPartners(@Param("businessName") String businessName,
                                @Param("city") String city,
                                @Param("status") PartnerStatus status,
                                @Param("type") PartnerType type,
                                Pageable pageable);
}
