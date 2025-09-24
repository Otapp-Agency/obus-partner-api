package com.obuspartners.api.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.obuspartners.modules.common.util.PageResponseWrapper;
import com.obuspartners.modules.common.util.ResponseWrapper;
import com.obuspartners.modules.partner_management.domain.dto.*;
import com.obuspartners.modules.partner_management.domain.enums.PartnerStatus;
import com.obuspartners.modules.partner_management.domain.enums.PartnerTier;
import com.obuspartners.modules.partner_management.service.PartnerService;

/**
 * REST Controller for Admin Partner Management API endpoints
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/admin/v1/partners")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminPartnerController {

    private final PartnerService partnerService;

    /**
     * Create a new partner (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> createPartner(
            @Valid @RequestBody CreatePartnerRequestDto createRequest) {
        PartnerResponseDto partner = partnerService.createPartner(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseWrapper<>(true, 201, "Partner created successfully", partner));
    }

    /**
     * Get all partners with pagination (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponseWrapper<PartnerSummaryDto>> getAllPartners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PartnerSummaryDto> partners = partnerService.getAllPartners(pageable);
        return ResponseEntity.ok(PageResponseWrapper.fromPage(partners, "Partners retrieved successfully"));
    }

    /**
     * Get partner by ID (Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> getPartnerById(@PathVariable Long id) {
        return partnerService.getPartnerById(id)
                .map(partner -> ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner retrieved successfully", partner)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseWrapper<>(false, 404, "Partner not found", null)));
    }

    /**
     * Get partner by UID (Admin only)
     */
    @GetMapping("/uid/{uid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> getPartnerByUid(@PathVariable String uid) {
        return partnerService.getPartnerByUid(uid)
                .map(partner -> ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner retrieved successfully", partner)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseWrapper<>(false, 404, "Partner not found", null)));
    }

    /**
     * Get partner by partner code (Admin only)
     */
    @GetMapping("/code/{partnerCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> getPartnerByCode(@PathVariable String partnerCode) {
        return partnerService.getPartnerByCode(partnerCode)
                .map(partner -> ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner retrieved successfully", partner)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseWrapper<>(false, 404, "Partner not found", null)));
    }

    /**
     * Update partner (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> updatePartner(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePartnerRequestDto updateRequest) {
        PartnerResponseDto partner = partnerService.updatePartner(id, updateRequest);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner updated successfully", partner));
    }

    /**
     * Update partner by UID (Admin only)
     */
    @PutMapping("/uid/{uid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> updatePartnerByUid(
            @PathVariable String uid,
            @Valid @RequestBody UpdatePartnerRequestDto updateRequest) {
        PartnerResponseDto partner = partnerService.updatePartnerByUid(uid, updateRequest);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner updated successfully", partner));
    }


    /**
     * Soft delete partner (mark as inactive) (Admin only)
     */
    @PutMapping("/{id}/soft-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> softDeletePartner(@PathVariable Long id) {
        PartnerResponseDto partner = partnerService.softDeletePartner(id);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner soft deleted successfully", partner));
    }

    /**
     * Soft delete partner by UID (mark as inactive) (Admin only)
     */
    @PutMapping("/uid/{uid}/soft-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> softDeletePartnerByUid(@PathVariable String uid) {
        PartnerResponseDto partner = partnerService.softDeletePartnerByUid(uid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner soft deleted successfully", partner));
    }

    /**
     * Activate partner (Admin only)
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> activatePartner(@PathVariable Long id) {
        PartnerResponseDto partner = partnerService.activatePartner(id);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner activated successfully", partner));
    }

    /**
     * Activate partner by UID (Admin only)
     */
    @PutMapping("/uid/{uid}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> activatePartnerByUid(@PathVariable String uid) {
        PartnerResponseDto partner = partnerService.activatePartnerByUid(uid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner activated successfully", partner));
    }

    /**
     * Deactivate partner (Admin only)
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> deactivatePartner(@PathVariable Long id) {
        PartnerResponseDto partner = partnerService.deactivatePartner(id);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner deactivated successfully", partner));
    }

    /**
     * Deactivate partner by UID (Admin only)
     */
    @PutMapping("/uid/{uid}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> deactivatePartnerByUid(@PathVariable String uid) {
        PartnerResponseDto partner = partnerService.deactivatePartnerByUid(uid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner deactivated successfully", partner));
    }

    /**
     * Verify partner (Admin only)
     */
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> verifyPartner(@PathVariable Long id) {
        PartnerResponseDto partner = partnerService.verifyPartner(id);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner verified successfully", partner));
    }

    /**
     * Verify partner by UID (Admin only)
     */
    @PutMapping("/uid/{uid}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> verifyPartnerByUid(@PathVariable String uid) {
        PartnerResponseDto partner = partnerService.verifyPartnerByUid(uid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner verified successfully", partner));
    }

    /**
     * Unverify partner (Admin only)
     */
    @PutMapping("/{id}/unverify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> unverifyPartner(@PathVariable Long id) {
        PartnerResponseDto partner = partnerService.unverifyPartner(id);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner unverified successfully", partner));
    }

    /**
     * Unverify partner by UID (Admin only)
     */
    @PutMapping("/uid/{uid}/unverify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> unverifyPartnerByUid(@PathVariable String uid) {
        PartnerResponseDto partner = partnerService.unverifyPartnerByUid(uid);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner unverified successfully", partner));
    }

    /**
     * Update partner status (Admin only)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> updatePartnerStatus(
            @PathVariable Long id,
            @RequestParam PartnerStatus status) {
        PartnerResponseDto partner = partnerService.updatePartnerStatus(id, status);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner status updated successfully", partner));
    }

    /**
     * Update partner status by UID (Admin only)
     */
    @PutMapping("/uid/{uid}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> updatePartnerStatusByUid(
            @PathVariable String uid,
            @RequestParam PartnerStatus status) {
        PartnerResponseDto partner = partnerService.updatePartnerStatusByUid(uid, status);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner status updated successfully", partner));
    }

    /**
     * Update partner tier (Admin only)
     */
    @PutMapping("/{id}/tier")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> updatePartnerTier(
            @PathVariable Long id,
            @RequestParam PartnerTier tier) {
        PartnerResponseDto partner = partnerService.updatePartnerTier(id, tier);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner tier updated successfully", partner));
    }

    /**
     * Update partner tier by UID (Admin only)
     */
    @PutMapping("/uid/{uid}/tier")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> updatePartnerTierByUid(
            @PathVariable String uid,
            @RequestParam PartnerTier tier) {
        PartnerResponseDto partner = partnerService.updatePartnerTierByUid(uid, tier);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner tier updated successfully", partner));
    }

    /**
     * Update partner commission rate (Admin only)
     */
    @PutMapping("/{id}/commission")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> updateCommissionRate(
            @PathVariable Long id,
            @RequestParam Double commissionRate) {
        PartnerResponseDto partner = partnerService.updateCommissionRate(id, commissionRate);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner commission rate updated successfully", partner));
    }

    /**
     * Update partner commission rate by UID (Admin only)
     */
    @PutMapping("/uid/{uid}/commission")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerResponseDto>> updateCommissionRateByUid(
            @PathVariable String uid,
            @RequestParam Double commissionRate) {
        PartnerResponseDto partner = partnerService.updateCommissionRateByUid(uid, commissionRate);
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner commission rate updated successfully", partner));
    }

    /**
     * Search partners (Admin only)
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponseWrapper<PartnerSummaryDto>> searchPartners(
            @Valid @RequestBody PartnerSearchRequestDto searchRequest) {
        
        Page<PartnerSummaryDto> partners = partnerService.searchPartners(searchRequest);
        return ResponseEntity.ok(PageResponseWrapper.fromPage(partners, "Partners search completed successfully"));
    }

    /**
     * Get partner statistics (Admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PartnerService.PartnerStatistics>> getPartnerStatistics() {
        PartnerService.PartnerStatistics statistics = partnerService.getPartnerStatistics();
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partner statistics retrieved successfully", statistics));
    }

    /**
     * Get partners by status (Admin only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponseWrapper<PartnerSummaryDto>> getPartnersByStatus(
            @PathVariable PartnerStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PartnerSummaryDto> partners = partnerService.getPartnersByStatus(status, pageable);
        return ResponseEntity.ok(PageResponseWrapper.fromPage(partners, "Partners retrieved by status successfully"));
    }

    /**
     * Get partners by tier (Admin only)
     */
    @GetMapping("/tier/{tier}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponseWrapper<PartnerSummaryDto>> getPartnersByTier(
            @PathVariable PartnerTier tier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PartnerSummaryDto> partners = partnerService.getPartnersByTier(tier, pageable);
        return ResponseEntity.ok(PageResponseWrapper.fromPage(partners, "Partners retrieved by tier successfully"));
    }

    /**
     * Bulk update partner status (Admin only)
     */
    @PutMapping("/bulk/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<String>> bulkUpdatePartnerStatus(
            @RequestBody BulkUpdateStatusRequestDto bulkRequest) {
        partnerService.bulkUpdatePartnerStatus(bulkRequest.getPartnerIds(), bulkRequest.getStatus());
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partners status updated successfully", null));
    }

    /**
     * Bulk update partner tier (Admin only)
     */
    @PutMapping("/bulk/tier")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<String>> bulkUpdatePartnerTier(
            @RequestBody BulkUpdateTierRequestDto bulkRequest) {
        partnerService.bulkUpdatePartnerTier(bulkRequest.getPartnerIds(), bulkRequest.getTier());
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Partners tier updated successfully", null));
    }
}
