package com.obuspartners.modules.partner_management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import com.obuspartners.modules.common.exception.DuplicateResourceException;
import com.obuspartners.modules.common.exception.ResourceNotFoundException;
import com.obuspartners.modules.partner_management.domain.dto.*;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.domain.enums.PartnerStatus;
import com.obuspartners.modules.partner_management.domain.enums.PartnerTier;
import com.obuspartners.modules.partner_management.domain.enums.PartnerType;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.repository.UserRepository;

import java.util.Optional;

/**
 * Implementation of PartnerService for managing partner operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PartnerResponseDto createPartner(CreatePartnerRequestDto createRequest) {
        log.info("Creating new partner with business name: {}", createRequest.getBusinessName());
        
        // Validate uniqueness of critical fields
        validateUniqueFieldsForCreation(createRequest);
        
        // Create partner entity
        Partner partner = new Partner();
        partner.setCode(createRequest.getCode());
        partner.setBusinessName(createRequest.getBusinessName());
        partner.setLegalName(createRequest.getLegalName());
        partner.setEmail(createRequest.getEmail());
        partner.setPhoneNumber(createRequest.getPhoneNumber());
        partner.setBusinessRegistrationNumber(createRequest.getBusinessRegistrationNumber());
        partner.setTaxIdentificationNumber(createRequest.getTaxIdentificationNumber());
        partner.setBusinessAddress(createRequest.getBusinessAddress());
        partner.setCity(createRequest.getCity());
        partner.setState(createRequest.getState());
        partner.setCountry(createRequest.getCountry());
        partner.setPostalCode(createRequest.getPostalCode());
        partner.setType(createRequest.getType());
        partner.setContactPersonName(createRequest.getContactPersonName());
        partner.setContactPersonEmail(createRequest.getContactPersonEmail());
        partner.setContactPersonPhone(createRequest.getContactPersonPhone());
        partner.setCommissionRate(createRequest.getCommissionRate());
        partner.setDescription(createRequest.getDescription());
        partner.setNotes(createRequest.getNotes());
        partner.setStatus(PartnerStatus.PENDING_VERIFICATION);
        partner.setTier(PartnerTier.BRONZE);
        partner.setIsActive(true);
        partner.setIsVerified(false); 

        // Set audit fields (timestamps are automatically set by @PrePersist)
        User currentUser = getCurrentUser();
        partner.setCreatedBy(currentUser);
        partner.setUpdatedBy(currentUser);

        // Save partner
        Partner savedPartner = partnerRepository.save(partner);
        log.info("Successfully created partner with ID: {} and code: {}", savedPartner.getId(), savedPartner.getCode());
        
        return convertToResponseDto(savedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto updatePartner(Long partnerId, UpdatePartnerRequestDto updateRequest) {
        log.info("Updating partner with ID: {}", partnerId);
        
        Partner partner = findPartnerById(partnerId);
        
        // Validate uniqueness of critical fields if they are being updated
        validateUniqueFieldsForUpdate(partner, updateRequest);
        
        // Update fields
        updatePartnerFields(partner, updateRequest);
        
        // Set updated by user (timestamp is automatically set by @PreUpdate)
        User currentUser = getCurrentUser();
        partner.setUpdatedBy(currentUser);
        
        // Save updated partner
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully updated partner with ID: {}", updatedPartner.getId());
        
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto updatePartnerByUid(String uid, UpdatePartnerRequestDto updateRequest) {
        log.info("Updating partner with UID: {}", uid);
        
        // Find partner by UID
        Partner partner = partnerRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with UID: " + uid));
        
        // Validate uniqueness of critical fields if they are being updated
        validateUniqueFieldsForUpdate(partner, updateRequest);
        
        // Update fields
        updatePartnerFields(partner, updateRequest);
        
        // Set updated by user (timestamp is automatically set by @PreUpdate)
        User currentUser = getCurrentUser();
        partner.setUpdatedBy(currentUser);
        
        // Save updated partner
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully updated partner with UID: {}", updatedPartner.getUid());
        
        return convertToResponseDto(updatedPartner);
    }

    @Override
    public Optional<PartnerResponseDto> getPartnerById(Long partnerId) {
        log.debug("Retrieving partner by ID: {}", partnerId);
        return partnerRepository.findById(partnerId)
                .map(this::convertToResponseDto);
    }

    @Override
    public Optional<PartnerResponseDto> getPartnerByUid(String uid) {
        log.debug("Retrieving partner by UID: {}", uid);
        return partnerRepository.findByUid(uid)
                .map(this::convertToResponseDto);
    }

    @Override
    public Optional<PartnerResponseDto> getPartnerByCode(String partnerCode) {
        log.debug("Retrieving partner by code: {}", partnerCode);
        return partnerRepository.findByCode(partnerCode)
                .map(this::convertToResponseDto);
    }

    @Override
    public Optional<PartnerResponseDto> getPartnerByEmail(String email) {
        log.debug("Retrieving partner by email: {}", email);
        return partnerRepository.findByEmail(email)
                .map(this::convertToResponseDto);
    }


    @Override
    @Transactional
    public PartnerResponseDto softDeletePartner(Long partnerId) {
        log.info("Soft deleting partner with ID: {}", partnerId);
        Partner partner = findPartnerById(partnerId);
        partner.setIsActive(false);
        partner.setStatus(PartnerStatus.INACTIVE);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully soft deleted partner with ID: {}", partnerId);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto softDeletePartnerByUid(String uid) {
        log.info("Soft deleting partner with UID: {}", uid);
        Partner partner = partnerRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with UID: " + uid));
        partner.setIsActive(false);
        partner.setStatus(PartnerStatus.INACTIVE);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully soft deleted partner with UID: {}", uid);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto activatePartnerByUid(String uid) {
        log.info("Activating partner with UID: {}", uid);
        Partner partner = partnerRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with UID: " + uid));
        partner.setIsActive(true);
        partner.setStatus(PartnerStatus.ACTIVE);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully activated partner with UID: {}", uid);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto activatePartner(Long partnerId) {
        log.info("Activating partner with ID: {}", partnerId);
        Partner partner = findPartnerById(partnerId);
        partner.setIsActive(true);
        partner.setStatus(PartnerStatus.ACTIVE);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully activated partner with ID: {}", partnerId);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto deactivatePartner(Long partnerId) {
        log.info("Deactivating partner with ID: {}", partnerId);
        Partner partner = findPartnerById(partnerId);
        partner.setIsActive(false);
        partner.setStatus(PartnerStatus.INACTIVE);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully deactivated partner with ID: {}", partnerId);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto deactivatePartnerByUid(String uid) {
        log.info("Deactivating partner with UID: {}", uid);
        Partner partner = partnerRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with UID: " + uid));
        partner.setIsActive(false);
        partner.setStatus(PartnerStatus.INACTIVE);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully deactivated partner with UID: {}", uid);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto verifyPartner(Long partnerId) {
        log.info("Verifying partner with ID: {}", partnerId);
        Partner partner = findPartnerById(partnerId);
        partner.setIsVerified(true);
        if (partner.getStatus() == PartnerStatus.PENDING_VERIFICATION) {
            partner.setStatus(PartnerStatus.ACTIVE);
        }
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully verified partner with ID: {}", partnerId);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto verifyPartnerByUid(String uid) {
        log.info("Verifying partner with UID: {}", uid);
        Partner partner = partnerRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with UID: " + uid));
        partner.setIsVerified(true);
        if (partner.getStatus() == PartnerStatus.PENDING_VERIFICATION) {
            partner.setStatus(PartnerStatus.ACTIVE);
        }
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully verified partner with UID: {}", uid);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto unverifyPartner(Long partnerId) {
        log.info("Unverifying partner with ID: {}", partnerId);
        Partner partner = findPartnerById(partnerId);
        partner.setIsVerified(false);
        partner.setStatus(PartnerStatus.PENDING_VERIFICATION);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully unverified partner with ID: {}", partnerId);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto unverifyPartnerByUid(String uid) {
        log.info("Unverifying partner with UID: {}", uid);
        Partner partner = partnerRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with UID: " + uid));
        partner.setIsVerified(false);
        partner.setStatus(PartnerStatus.PENDING_VERIFICATION);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully unverified partner with UID: {}", uid);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto updatePartnerStatus(Long partnerId, PartnerStatus status) {
        log.info("Updating partner status to {} for partner ID: {}", status, partnerId);
        Partner partner = findPartnerById(partnerId);
        partner.setStatus(status);
        
        // Update isActive based on status
        if (status == PartnerStatus.ACTIVE) {
            partner.setIsActive(true);
        } else if (status == PartnerStatus.INACTIVE || status == PartnerStatus.SUSPENDED || 
                   status == PartnerStatus.TERMINATED) {
            partner.setIsActive(false);
        }
        
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully updated partner status to {} for partner ID: {}", status, partnerId);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto updatePartnerStatusByUid(String uid, PartnerStatus status) {
        log.info("Updating partner status to {} for partner UID: {}", status, uid);
        Partner partner = partnerRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with UID: " + uid));
        partner.setStatus(status);
        
        // Update isActive based on status
        if (status == PartnerStatus.ACTIVE) {
            partner.setIsActive(true);
        } else if (status == PartnerStatus.INACTIVE || status == PartnerStatus.SUSPENDED || 
                   status == PartnerStatus.TERMINATED) {
            partner.setIsActive(false);
        }
        
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully updated partner status to {} for partner UID: {}", status, uid);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto updatePartnerTier(Long partnerId, PartnerTier tier) {
        log.info("Updating partner tier to {} for partner ID: {}", tier, partnerId);
        Partner partner = findPartnerById(partnerId);
        partner.setTier(tier);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully updated partner tier to {} for partner ID: {}", tier, partnerId);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto updatePartnerTierByUid(String uid, PartnerTier tier) {
        log.info("Updating partner tier to {} for partner UID: {}", tier, uid);
        Partner partner = partnerRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with UID: " + uid));
        partner.setTier(tier);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully updated partner tier to {} for partner UID: {}", tier, uid);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto updateCommissionRate(Long partnerId, Double commissionRate) {
        log.info("Updating commission rate to {} for partner ID: {}", commissionRate, partnerId);
        Partner partner = findPartnerById(partnerId);
        partner.setCommissionRate(commissionRate);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully updated commission rate to {} for partner ID: {}", commissionRate, partnerId);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    @Transactional
    public PartnerResponseDto updateCommissionRateByUid(String uid, Double commissionRate) {
        log.info("Updating commission rate to {} for partner UID: {}", commissionRate, uid);
        Partner partner = partnerRepository.findByUid(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with UID: " + uid));
        partner.setCommissionRate(commissionRate);
        partner.setUpdatedBy(getCurrentUser());
        Partner updatedPartner = partnerRepository.save(partner);
        log.info("Successfully updated commission rate to {} for partner UID: {}", commissionRate, uid);
        return convertToResponseDto(updatedPartner);
    }

    @Override
    public Page<PartnerSummaryDto> getAllPartners(Pageable pageable) {
        log.debug("Retrieving all partners with pagination: {}", pageable);
        return partnerRepository.findAll(pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public List<PartnerSummaryDto> getAllPartnersForAssignment() {
        log.debug("Retrieving all active partners for assignment (non-paginated)");
        return partnerRepository.findAll()
                .stream()
                .filter(partner -> partner.getIsActive() && partner.getStatus() == PartnerStatus.ACTIVE)
                .map(this::convertToSummaryDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Page<PartnerSummaryDto> getPartnersByStatus(PartnerStatus status, Pageable pageable) {
        log.debug("Retrieving partners by status: {} with pagination: {}", status, pageable);
        return partnerRepository.findByStatus(status, pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public Page<PartnerSummaryDto> getPartnersByType(PartnerType type, Pageable pageable) {
        log.debug("Retrieving partners by type: {} with pagination: {}", type, pageable);
        return partnerRepository.findByType(type, pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public Page<PartnerSummaryDto> getPartnersByTier(PartnerTier tier, Pageable pageable) {
        log.debug("Retrieving partners by tier: {} with pagination: {}", tier, pageable);
        return partnerRepository.findByTier(tier, pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public Page<PartnerSummaryDto> getActivePartners(Pageable pageable) {
        log.debug("Retrieving active partners with pagination: {}", pageable);
        return partnerRepository.findByIsActiveTrue(pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public Page<PartnerSummaryDto> getVerifiedPartners(Pageable pageable) {
        log.debug("Retrieving verified partners with pagination: {}", pageable);
        return partnerRepository.findByIsVerifiedTrue(pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public Page<PartnerSummaryDto> getPartnersByCity(String city, Pageable pageable) {
        log.debug("Retrieving partners by city: {} with pagination: {}", city, pageable);
        return partnerRepository.findByCity(city, pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public Page<PartnerSummaryDto> getPartnersByState(String state, Pageable pageable) {
        log.debug("Retrieving partners by state: {} with pagination: {}", state, pageable);
        return partnerRepository.findByState(state, pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public Page<PartnerSummaryDto> getPartnersByCountry(String country, Pageable pageable) {
        log.debug("Retrieving partners by country: {} with pagination: {}", country, pageable);
        return partnerRepository.findByCountry(country, pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public Page<PartnerSummaryDto> searchPartnersByBusinessName(String businessName, Pageable pageable) {
        log.debug("Searching partners by business name: {} with pagination: {}", businessName, pageable);
        return partnerRepository.findByBusinessNameContainingIgnoreCase(businessName, pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public Page<PartnerSummaryDto> searchPartnersByLegalName(String legalName, Pageable pageable) {
        log.debug("Searching partners by legal name: {} with pagination: {}", legalName, pageable);
        return partnerRepository.findByLegalNameContainingIgnoreCase(legalName, pageable)
                .map(this::convertToSummaryDto);
    }

    @Override
    public Page<PartnerSummaryDto> searchPartners(PartnerSearchRequestDto searchRequest) {
        log.debug("Advanced search partners with criteria: {}", searchRequest);
        
        // Create pageable with sorting
        Pageable pageable = createPageableFromSearchRequest(searchRequest);
        
        // Use repository search method
        return partnerRepository.searchPartners(
                searchRequest.getBusinessName(),
                searchRequest.getCity(),
                searchRequest.getStatus(),
                searchRequest.getType(),
                pageable
        ).map(this::convertToSummaryDto);
    }

    @Override
    public boolean existsByUid(String uid) {
        log.debug("Checking if UID exists: {}", uid);
        return partnerRepository.existsByUid(uid);
    }

    @Override
    public boolean existsByCode(String partnerCode) {
        log.debug("Checking if partner code exists: {}", partnerCode);
        return partnerRepository.existsByCode(partnerCode);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return partnerRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        log.debug("Checking if phone number exists: {}", phoneNumber);
        return partnerRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber) {
        log.debug("Checking if business registration number exists: {}", businessRegistrationNumber);
        return partnerRepository.existsByBusinessRegistrationNumber(businessRegistrationNumber);
    }

    @Override
    public boolean existsByTaxIdentificationNumber(String taxIdentificationNumber) {
        log.debug("Checking if tax identification number exists: {}", taxIdentificationNumber);
        return partnerRepository.existsByTaxIdentificationNumber(taxIdentificationNumber);
    }

    @Override
    public boolean validatePartner(Partner partner) {
        log.debug("Validating partner: {}", partner.getCode());
        
        // Basic validation checks
        if (!StringUtils.hasText(partner.getBusinessName())) {
            log.warn("Partner validation failed: Business name is required");
            return false;
        }
        
        if (!StringUtils.hasText(partner.getEmail())) {
            log.warn("Partner validation failed: Email is required");
            return false;
        }
        
        if (!StringUtils.hasText(partner.getPhoneNumber())) {
            log.warn("Partner validation failed: Phone number is required");
            return false;
        }
        
        // Email format validation
        if (!isValidEmail(partner.getEmail())) {
            log.warn("Partner validation failed: Invalid email format");
            return false;
        }
        
        // Phone number validation
        if (!isValidPhoneNumber(partner.getPhoneNumber())) {
            log.warn("Partner validation failed: Invalid phone number format");
            return false;
        }
        
        log.debug("Partner validation successful: {}", partner.getCode());
        return true;
    }

    @Override
    public String generatePartnerCode() {
        log.debug("Generating unique partner code");
        String baseCode = "PTR" + System.currentTimeMillis();
        String uniqueCode = baseCode;
        
        int counter = 1;
        while (partnerRepository.existsByCode(uniqueCode)) {
            uniqueCode = baseCode + "_" + counter;
            counter++;
        }
        
        log.debug("Generated unique partner code: {}", uniqueCode);
        return uniqueCode;
    }

    @Override
    public long countPartnersByStatus(PartnerStatus status) {
        log.debug("Counting partners by status: {}", status);
        return partnerRepository.countByStatus(status);
    }

    @Override
    public long countPartnersByType(PartnerType type) {
        log.debug("Counting partners by type: {}", type);
        return partnerRepository.countByType(type);
    }

    @Override
    public long countPartnersByTier(PartnerTier tier) {
        log.debug("Counting partners by tier: {}", tier);
        return partnerRepository.countByTier(tier);
    }

    @Override
    public long countActivePartners() {
        log.debug("Counting active partners");
        return partnerRepository.countByIsActiveTrue();
    }

    @Override
    public long countVerifiedPartners() {
        log.debug("Counting verified partners");
        return partnerRepository.countByIsVerifiedTrue();
    }

    @Override
    public PartnerStatistics getPartnerStatistics() {
        log.debug("Retrieving partner statistics");
        
        long totalPartners = partnerRepository.count();
        long activePartners = countActivePartners();
        long verifiedPartners = countVerifiedPartners();
        long corporatePartners = countPartnersByType(PartnerType.CORPORATE);
        long goldTierPartners = countPartnersByTier(PartnerTier.GOLD);
        
        return new PartnerStatistics(totalPartners, activePartners, verifiedPartners, 
                                    corporatePartners, goldTierPartners);
    }


    // Private helper methods

    private Partner findPartnerById(Long partnerId) {
        return partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + partnerId));
    }


    private void updatePartnerFields(Partner partner, UpdatePartnerRequestDto updateRequest) {
        if (StringUtils.hasText(updateRequest.getBusinessName())) {
            partner.setBusinessName(updateRequest.getBusinessName());
        }
        
        if (StringUtils.hasText(updateRequest.getLegalName())) {
            partner.setLegalName(updateRequest.getLegalName());
        }
        
        if (StringUtils.hasText(updateRequest.getEmail())) {
            partner.setEmail(updateRequest.getEmail());
        }
        
        if (StringUtils.hasText(updateRequest.getPhoneNumber())) {
            partner.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        
        if (StringUtils.hasText(updateRequest.getBusinessRegistrationNumber())) {
            partner.setBusinessRegistrationNumber(updateRequest.getBusinessRegistrationNumber());
        }
        
        if (StringUtils.hasText(updateRequest.getTaxIdentificationNumber())) {
            partner.setTaxIdentificationNumber(updateRequest.getTaxIdentificationNumber());
        }
        
        if (StringUtils.hasText(updateRequest.getBusinessAddress())) {
            partner.setBusinessAddress(updateRequest.getBusinessAddress());
        }
        
        if (StringUtils.hasText(updateRequest.getCity())) {
            partner.setCity(updateRequest.getCity());
        }
        
        if (StringUtils.hasText(updateRequest.getState())) {
            partner.setState(updateRequest.getState());
        }
        
        if (StringUtils.hasText(updateRequest.getCountry())) {
            partner.setCountry(updateRequest.getCountry());
        }
        
        if (StringUtils.hasText(updateRequest.getPostalCode())) {
            partner.setPostalCode(updateRequest.getPostalCode());
        }
        
        if (updateRequest.getStatus() != null) {
            partner.setStatus(updateRequest.getStatus());
        }
        
        if (updateRequest.getType() != null) {
            partner.setType(updateRequest.getType());
        }
        
        if (updateRequest.getTier() != null) {
            partner.setTier(updateRequest.getTier());
        }
        
        if (updateRequest.getIsActive() != null) {
            partner.setIsActive(updateRequest.getIsActive());
        }
        
        if (updateRequest.getIsVerified() != null) {
            partner.setIsVerified(updateRequest.getIsVerified());
        }
        
        if (StringUtils.hasText(updateRequest.getContactPersonName())) {
            partner.setContactPersonName(updateRequest.getContactPersonName());
        }
        
        if (StringUtils.hasText(updateRequest.getContactPersonEmail())) {
            partner.setContactPersonEmail(updateRequest.getContactPersonEmail());
        }
        
        if (StringUtils.hasText(updateRequest.getContactPersonPhone())) {
            partner.setContactPersonPhone(updateRequest.getContactPersonPhone());
        }
        
        if (updateRequest.getCommissionRate() != null) {
            partner.setCommissionRate(updateRequest.getCommissionRate());
        }
        
        if (StringUtils.hasText(updateRequest.getDescription())) {
            partner.setDescription(updateRequest.getDescription());
        }
        
        if (StringUtils.hasText(updateRequest.getNotes())) {
            partner.setNotes(updateRequest.getNotes());
        }
    }

    private PartnerResponseDto convertToResponseDto(Partner partner) {
        return PartnerResponseDto.builder()
                .id(partner.getId())
                .uid(partner.getUid())
                .code(partner.getCode())
                .businessName(partner.getBusinessName())
                .legalName(partner.getLegalName())
                .email(partner.getEmail())
                .phoneNumber(partner.getPhoneNumber())
                .businessRegistrationNumber(partner.getBusinessRegistrationNumber())
                .taxIdentificationNumber(partner.getTaxIdentificationNumber())
                .businessAddress(partner.getBusinessAddress())
                .city(partner.getCity())
                .state(partner.getState())
                .country(partner.getCountry())
                .postalCode(partner.getPostalCode())
                .status(partner.getStatus())
                .type(partner.getType())
                .tier(partner.getTier())
                .isActive(partner.getIsActive())
                .isVerified(partner.getIsVerified())
                .commissionRate(partner.getCommissionRate())
                .contactPersonName(partner.getContactPersonName())
                .contactPersonEmail(partner.getContactPersonEmail())
                .contactPersonPhone(partner.getContactPersonPhone())
                .description(partner.getDescription())
                .notes(partner.getNotes())
                .createdAt(partner.getCreatedAt())
                .updatedAt(partner.getUpdatedAt())
                .createdByUsername(partner.getCreatedBy() != null ? partner.getCreatedBy().getUsername() : null)
                .createdByEmail(partner.getCreatedBy() != null ? partner.getCreatedBy().getEmail() : null)
                .updatedByUsername(partner.getUpdatedBy() != null ? partner.getUpdatedBy().getUsername() : null)
                .updatedByEmail(partner.getUpdatedBy() != null ? partner.getUpdatedBy().getEmail() : null)
                .build();
    }

    private PartnerSummaryDto convertToSummaryDto(Partner partner) {
        return PartnerSummaryDto.builder()
                .id(partner.getId())
                .uid(partner.getUid())
                .code(partner.getCode())
                .businessName(partner.getBusinessName())
                .legalName(partner.getLegalName())
                .email(partner.getEmail())
                .phoneNumber(partner.getPhoneNumber())
                .city(partner.getCity())
                .state(partner.getState())
                .country(partner.getCountry())
                .status(partner.getStatus())
                .type(partner.getType())
                .tier(partner.getTier())
                .isActive(partner.getIsActive())
                .isVerified(partner.getIsVerified())
                .commissionRate(partner.getCommissionRate())
                .contactPersonName(partner.getContactPersonName())
                .createdAt(partner.getCreatedAt())
                .build();
    }

    private Pageable createPageableFromSearchRequest(PartnerSearchRequestDto searchRequest) {
        Sort sort = Sort.unsorted();
        
        if (StringUtils.hasText(searchRequest.getSortBy())) {
            Sort.Direction direction = StringUtils.hasText(searchRequest.getSortDirection()) && 
                                     "DESC".equalsIgnoreCase(searchRequest.getSortDirection()) 
                                     ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, searchRequest.getSortBy());
        }
        
        return PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
    }


    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^[+]?[0-9\\s\\-()]{10,20}$");
    }

    @Override
    public void bulkUpdatePartnerStatus(List<Long> partnerIds, PartnerStatus status) {
        log.info("Bulk updating partner status for {} partners to {}", partnerIds.size(), status);
        
        if (partnerIds == null || partnerIds.isEmpty()) {
            log.warn("Bulk update failed: No partner IDs provided");
            return;
        }
        
        User currentUser = getCurrentUser();
        
        for (Long partnerId : partnerIds) {
            try {
                Partner partner = partnerRepository.findById(partnerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + partnerId));
                
                partner.setStatus(status);
                partner.setUpdatedBy(currentUser);
                partnerRepository.save(partner);
                
                log.debug("Updated partner {} status to {}", partner.getCode(), status);
            } catch (Exception e) {
                log.error("Failed to update partner {} status: {}", partnerId, e.getMessage());
                // Continue with other partners even if one fails
            }
        }
        
        log.info("Bulk update completed for {} partners", partnerIds.size());
    }

    @Override
    public void bulkUpdatePartnerTier(List<Long> partnerIds, PartnerTier tier) {
        log.info("Bulk updating partner tier for {} partners to {}", partnerIds.size(), tier);
        
        if (partnerIds == null || partnerIds.isEmpty()) {
            log.warn("Bulk update failed: No partner IDs provided");
            return;
        }
        
        User currentUser = getCurrentUser();
        
        for (Long partnerId : partnerIds) {
            try {
                Partner partner = partnerRepository.findById(partnerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + partnerId));
                
                partner.setTier(tier);
                partner.setUpdatedBy(currentUser);
                partnerRepository.save(partner);
                
                log.debug("Updated partner {} tier to {}", partner.getCode(), tier);
            } catch (Exception e) {
                log.error("Failed to update partner {} tier: {}", partnerId, e.getMessage());
                // Continue with other partners even if one fails
            }
        }
        
        log.info("Bulk update completed for {} partners", partnerIds.size());
    }

    /**
     * Get the current authenticated user from security context
     * 
     * @return the current user
     * @throws ResourceNotFoundException if no user is authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    /**
     * Validate uniqueness of critical fields for partner creation
     */
    private void validateUniqueFieldsForCreation(CreatePartnerRequestDto createRequest) {
        // Check code uniqueness
        if (partnerRepository.existsByCode(createRequest.getCode())) {
            throw new DuplicateResourceException("Code already exists: " + createRequest.getCode());
        }
        
        // Check email uniqueness
        if (partnerRepository.existsByEmail(createRequest.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + createRequest.getEmail());
        }
        
        // Check phone number uniqueness
        if (partnerRepository.existsByPhoneNumber(createRequest.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already exists: " + createRequest.getPhoneNumber());
        }
        
        // Check business registration number uniqueness
        if (partnerRepository.existsByBusinessRegistrationNumber(createRequest.getBusinessRegistrationNumber())) {
            throw new DuplicateResourceException("Business registration number already exists: " + createRequest.getBusinessRegistrationNumber());
        }
        
        // Check tax identification number uniqueness
        if (partnerRepository.existsByTaxIdentificationNumber(createRequest.getTaxIdentificationNumber())) {
            throw new DuplicateResourceException("Tax identification number already exists: " + createRequest.getTaxIdentificationNumber());
        }
    }

    /**
     * Validate uniqueness of critical fields for partner update
     */
    private void validateUniqueFieldsForUpdate(Partner partner, UpdatePartnerRequestDto updateRequest) {
        // Check email uniqueness (if being updated)
        if (updateRequest.getEmail() != null && 
            !updateRequest.getEmail().equals(partner.getEmail()) &&
            partnerRepository.existsByEmail(updateRequest.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + updateRequest.getEmail());
        }
        
        // Check phone number uniqueness (if being updated)
        if (updateRequest.getPhoneNumber() != null && 
            !updateRequest.getPhoneNumber().equals(partner.getPhoneNumber()) &&
            partnerRepository.existsByPhoneNumber(updateRequest.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already exists: " + updateRequest.getPhoneNumber());
        }
        
        // Check business registration number uniqueness (if being updated)
        if (updateRequest.getBusinessRegistrationNumber() != null && 
            !updateRequest.getBusinessRegistrationNumber().equals(partner.getBusinessRegistrationNumber()) &&
            partnerRepository.existsByBusinessRegistrationNumber(updateRequest.getBusinessRegistrationNumber())) {
            throw new DuplicateResourceException("Business registration number already exists: " + updateRequest.getBusinessRegistrationNumber());
        }
        
        // Check tax identification number uniqueness (if being updated)
        if (updateRequest.getTaxIdentificationNumber() != null && 
            !updateRequest.getTaxIdentificationNumber().equals(partner.getTaxIdentificationNumber()) &&
            partnerRepository.existsByTaxIdentificationNumber(updateRequest.getTaxIdentificationNumber())) {
            throw new DuplicateResourceException("Tax identification number already exists: " + updateRequest.getTaxIdentificationNumber());
        }
    }
}
