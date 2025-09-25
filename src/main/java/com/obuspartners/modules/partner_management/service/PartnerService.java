package com.obuspartners.modules.partner_management.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.obuspartners.modules.partner_management.domain.dto.*;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.domain.enums.PartnerStatus;
import com.obuspartners.modules.partner_management.domain.enums.PartnerTier;
import com.obuspartners.modules.partner_management.domain.enums.PartnerType;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Partner management operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface PartnerService {

    /**
     * Create a new partner
     * 
     * @param createRequest the partner creation request
     * @return the created partner response
     */
    PartnerResponseDto createPartner(CreatePartnerRequestDto createRequest);

    /**
     * Update an existing partner
     * 
     * @param partnerId the ID of the partner to update
     * @param updateRequest the partner update request
     * @return the updated partner response
     */
    PartnerResponseDto updatePartner(Long partnerId, UpdatePartnerRequestDto updateRequest);

    /**
     * Update an existing partner by UID
     * 
     * @param uid the UID of the partner to update
     * @param updateRequest the partner update request
     * @return the updated partner response
     */
    PartnerResponseDto updatePartnerByUid(String uid, UpdatePartnerRequestDto updateRequest);

    /**
     * Get partner by ID
     * 
     * @param partnerId the partner ID
     * @return Optional containing the partner response if found
     */
    Optional<PartnerResponseDto> getPartnerById(Long partnerId);

    /**
     * Get partner by UID
     * 
     * @param uid the partner UID
     * @return Optional containing the partner response if found
     */
    Optional<PartnerResponseDto> getPartnerByUid(String uid);

    /**
     * Get partner by partner code
     * 
     * @param partnerCode the partner code
     * @return Optional containing the partner response if found
     */
    Optional<PartnerResponseDto> getPartnerByCode(String partnerCode);

    /**
     * Get partner by email
     * 
     * @param email the email address
     * @return Optional containing the partner response if found
     */
    Optional<PartnerResponseDto> getPartnerByEmail(String email);


    /**
     * Soft delete a partner (mark as inactive)
     * 
     * @param partnerId the ID of the partner to soft delete
     * @return the updated partner response
     */
    PartnerResponseDto softDeletePartner(Long partnerId);

    /**
     * Soft delete a partner by UID (mark as inactive)
     * 
     * @param uid the UID of the partner to soft delete
     * @return the updated partner response
     */
    PartnerResponseDto softDeletePartnerByUid(String uid);

    /**
     * Activate a partner
     * 
     * @param partnerId the ID of the partner to activate
     * @return the updated partner response
     */
    PartnerResponseDto activatePartner(Long partnerId);

    /**
     * Activate a partner by UID
     * 
     * @param uid the UID of the partner to activate
     * @return the updated partner response
     */
    PartnerResponseDto activatePartnerByUid(String uid);

    /**
     * Deactivate a partner
     * 
     * @param partnerId the ID of the partner to deactivate
     * @return the updated partner response
     */
    PartnerResponseDto deactivatePartner(Long partnerId);

    /**
     * Deactivate a partner by UID
     * 
     * @param uid the UID of the partner to deactivate
     * @return the updated partner response
     */
    PartnerResponseDto deactivatePartnerByUid(String uid);

    /**
     * Verify a partner
     * 
     * @param partnerId the ID of the partner to verify
     * @return the updated partner response
     */
    PartnerResponseDto verifyPartner(Long partnerId);

    /**
     * Verify a partner by UID
     * 
     * @param uid the UID of the partner to verify
     * @return the updated partner response
     */
    PartnerResponseDto verifyPartnerByUid(String uid);

    /**
     * Unverify a partner
     * 
     * @param partnerId the ID of the partner to unverify
     * @return the updated partner response
     */
    PartnerResponseDto unverifyPartner(Long partnerId);

    /**
     * Unverify a partner by UID
     * 
     * @param uid the UID of the partner to unverify
     * @return the updated partner response
     */
    PartnerResponseDto unverifyPartnerByUid(String uid);

    /**
     * Update partner status
     * 
     * @param partnerId the ID of the partner
     * @param status the new status
     * @return the updated partner response
     */
    PartnerResponseDto updatePartnerStatus(Long partnerId, PartnerStatus status);

    /**
     * Update partner status by UID
     * 
     * @param uid the UID of the partner
     * @param status the new status
     * @return the updated partner response
     */
    PartnerResponseDto updatePartnerStatusByUid(String uid, PartnerStatus status);

    /**
     * Update partner tier
     * 
     * @param partnerId the ID of the partner
     * @param tier the new tier
     * @return the updated partner response
     */
    PartnerResponseDto updatePartnerTier(Long partnerId, PartnerTier tier);

    /**
     * Update partner tier by UID
     * 
     * @param uid the UID of the partner
     * @param tier the new tier
     * @return the updated partner response
     */
    PartnerResponseDto updatePartnerTierByUid(String uid, PartnerTier tier);

    /**
     * Update partner commission rate
     * 
     * @param partnerId the ID of the partner
     * @param commissionRate the new commission rate
     * @return the updated partner response
     */
    PartnerResponseDto updateCommissionRate(Long partnerId, Double commissionRate);

    /**
     * Update partner commission rate by UID
     * 
     * @param uid the UID of the partner
     * @param commissionRate the new commission rate
     * @return the updated partner response
     */
    PartnerResponseDto updateCommissionRateByUid(String uid, Double commissionRate);

    /**
     * Get all partners with pagination
     * 
     * @param pageable pagination information
     * @return Page of partner summaries
     */
    Page<PartnerSummaryDto> getAllPartners(Pageable pageable);

    /**
     * Get all partners without pagination (for assignment purposes)
     * 
     * @return List of all partner summaries
     */
    List<PartnerSummaryDto> getAllPartnersForAssignment();

    /**
     * Get partners by status
     * 
     * @param status the partner status
     * @param pageable pagination information
     * @return Page of partner summaries with the specified status
     */
    Page<PartnerSummaryDto> getPartnersByStatus(PartnerStatus status, Pageable pageable);

    /**
     * Get partners by type
     * 
     * @param type the partner type
     * @param pageable pagination information
     * @return Page of partner summaries with the specified type
     */
    Page<PartnerSummaryDto> getPartnersByType(PartnerType type, Pageable pageable);

    /**
     * Get partners by tier
     * 
     * @param tier the partner tier
     * @param pageable pagination information
     * @return Page of partner summaries with the specified tier
     */
    Page<PartnerSummaryDto> getPartnersByTier(PartnerTier tier, Pageable pageable);

    /**
     * Get active partners
     * 
     * @param pageable pagination information
     * @return Page of active partner summaries
     */
    Page<PartnerSummaryDto> getActivePartners(Pageable pageable);

    /**
     * Get verified partners
     * 
     * @param pageable pagination information
     * @return Page of verified partner summaries
     */
    Page<PartnerSummaryDto> getVerifiedPartners(Pageable pageable);

    /**
     * Get partners by city
     * 
     * @param city the city name
     * @param pageable pagination information
     * @return Page of partner summaries in the specified city
     */
    Page<PartnerSummaryDto> getPartnersByCity(String city, Pageable pageable);

    /**
     * Get partners by state
     * 
     * @param state the state name
     * @param pageable pagination information
     * @return Page of partner summaries in the specified state
     */
    Page<PartnerSummaryDto> getPartnersByState(String state, Pageable pageable);

    /**
     * Get partners by country
     * 
     * @param country the country name
     * @param pageable pagination information
     * @return Page of partner summaries in the specified country
     */
    Page<PartnerSummaryDto> getPartnersByCountry(String country, Pageable pageable);

    /**
     * Search partners by business name
     * 
     * @param businessName the business name to search for
     * @param pageable pagination information
     * @return Page of partner summaries matching the search criteria
     */
    Page<PartnerSummaryDto> searchPartnersByBusinessName(String businessName, Pageable pageable);

    /**
     * Search partners by legal name
     * 
     * @param legalName the legal name to search for
     * @param pageable pagination information
     * @return Page of partner summaries matching the search criteria
     */
    Page<PartnerSummaryDto> searchPartnersByLegalName(String legalName, Pageable pageable);

    /**
     * Advanced search partners with multiple criteria
     * 
     * @param searchRequest the search request with criteria
     * @return Page of partner summaries matching the search criteria
     */
    Page<PartnerSummaryDto> searchPartners(PartnerSearchRequestDto searchRequest);

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
    boolean existsByCode(String code);

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
     * Validate partner data before creation
     * 
     * @param partner the partner to validate
     * @return true if valid, false otherwise
     */
    boolean validatePartner(Partner partner);

    /**
     * Generate unique partner code
     * 
     * @return a unique partner code
     */
    String generatePartnerCode();

    /**
     * Count partners by status
     * 
     * @param status the partner status
     * @return number of partners with the specified status
     */
    long countPartnersByStatus(PartnerStatus status);

    /**
     * Count partners by type
     * 
     * @param type the partner type
     * @return number of partners with the specified type
     */
    long countPartnersByType(PartnerType type);

    /**
     * Count partners by tier
     * 
     * @param tier the partner tier
     * @return number of partners with the specified tier
     */
    long countPartnersByTier(PartnerTier tier);

    /**
     * Count active partners
     * 
     * @return number of active partners
     */
    long countActivePartners();

    /**
     * Count verified partners
     * 
     * @return number of verified partners
     */
    long countVerifiedPartners();

    /**
     * Get partner statistics
     * 
     * @return partner statistics summary
     */
    PartnerStatistics getPartnerStatistics();

    /**
     * Bulk update partner status
     * 
     * @param partnerIds list of partner IDs to update
     * @param status the new status
     */
    void bulkUpdatePartnerStatus(java.util.List<Long> partnerIds, PartnerStatus status);

    /**
     * Bulk update partner tier
     * 
     * @param partnerIds list of partner IDs to update
     * @param tier the new tier
     */
    void bulkUpdatePartnerTier(java.util.List<Long> partnerIds, PartnerTier tier);


    /**
     * Partner statistics class
     */
    class PartnerStatistics {
        private long totalPartners;
        private long activePartners;
        private long verifiedPartners;
        private long partnersByType;
        private long partnersByTier;

        // Constructors, getters, and setters would be implemented in the service implementation
        public PartnerStatistics() {}

        public PartnerStatistics(long totalPartners, long activePartners, long verifiedPartners, 
                                long partnersByType, long partnersByTier) {
            this.totalPartners = totalPartners;
            this.activePartners = activePartners;
            this.verifiedPartners = verifiedPartners;
            this.partnersByType = partnersByType;
            this.partnersByTier = partnersByTier;
        }

        // Getters and setters
        public long getTotalPartners() { return totalPartners; }
        public void setTotalPartners(long totalPartners) { this.totalPartners = totalPartners; }
        
        public long getActivePartners() { return activePartners; }
        public void setActivePartners(long activePartners) { this.activePartners = activePartners; }
        
        public long getVerifiedPartners() { return verifiedPartners; }
        public void setVerifiedPartners(long verifiedPartners) { this.verifiedPartners = verifiedPartners; }
        
        public long getPartnersByType() { return partnersByType; }
        public void setPartnersByType(long partnersByType) { this.partnersByType = partnersByType; }
        
        public long getPartnersByTier() { return partnersByTier; }
        public void setPartnersByTier(long partnersByTier) { this.partnersByTier = partnersByTier; }
    }
}
