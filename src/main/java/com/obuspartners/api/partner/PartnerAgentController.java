package com.obuspartners.api.partner;

import com.obuspartners.modules.agent_management.domain.dto.*;
import com.obuspartners.modules.agent_management.service.AgentService;
import com.obuspartners.modules.agent_management.service.AgentVerificationService;
import com.obuspartners.modules.common.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for Partner Agent management operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/partner/v1/agents")
@RequiredArgsConstructor
@Tag(name = "Partner Agent Management", description = "Agent management operations for partners")
public class PartnerAgentController {

    private final AgentService agentService;
    private final AgentVerificationService agentVerificationService;

    /**
     * Submit an agent registration request with partner verification
     */
    @PostMapping("/register")
    @Operation(summary = "Submit agent registration request with partner verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Agent registration request submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key"),
        @ApiResponse(responseCode = "409", description = "Agent registration request already exists")
    })
    public ResponseEntity<ResponseWrapper<AgentRegistrationResponse>> registerAgent(
            @Valid @RequestBody CreateAgentRequestDto createRequest,
            @Parameter(description = "Partner-specific credentials for verification")
            @RequestParam Map<String, Object> credentials) {

        log.info("Registering agent with partner verification for partner: {}", credentials.get("partnerUid"));

        try {
            // Create agent
            AgentResponseDto agentResponse = agentService.createAgent(createRequest);
            
            // Request verification
            String verificationRequestId = agentVerificationService.requestAgentVerification(
                agentResponse.getUid(), credentials);

            // Create flattened response with agent data and verification info
            AgentRegistrationResponse response = new AgentRegistrationResponse();
            response.setId(agentResponse.getId());
            response.setUid(agentResponse.getUid());
            response.setAgentCode(agentResponse.getCode());
            response.setPartnerAgentNumber(agentResponse.getPartnerAgentNumber());
        response.setPassName(agentResponse.getPassName());
        // Note: passCode is sent via email for security reasons
            response.setBusinessName(agentResponse.getBusinessName());
            response.setContactPerson(agentResponse.getContactPerson());
            response.setPhoneNumber(agentResponse.getPhoneNumber());
            response.setBusinessEmail(agentResponse.getBusinessEmail());
            response.setBusinessAddress(agentResponse.getBusinessAddress());
            response.setTaxId(agentResponse.getTaxId());
            response.setLicenseNumber(agentResponse.getLicenseNumber());
            response.setAgentType(agentResponse.getAgentType() != null ? agentResponse.getAgentType().toString() : null);
            response.setStatus(agentResponse.getStatus() != null ? agentResponse.getStatus().toString() : null);
            response.setRegistrationDate(agentResponse.getRegistrationDate() != null ? agentResponse.getRegistrationDate().toString() : null);
            response.setApprovalDate(agentResponse.getApprovalDate() != null ? agentResponse.getApprovalDate().toString() : null);
            response.setLastActivityDate(agentResponse.getLastActivityDate() != null ? agentResponse.getLastActivityDate().toString() : null);
            response.setNotes(agentResponse.getNotes());
            response.setCreatedAt(agentResponse.getCreatedAt() != null ? agentResponse.getCreatedAt().toString() : null);
            response.setUpdatedAt(agentResponse.getUpdatedAt() != null ? agentResponse.getUpdatedAt().toString() : null);
            response.setPartnerId(agentResponse.getPartnerId());
            response.setPartnerUid(agentResponse.getPartnerUid());
            response.setPartnerCode(agentResponse.getPartnerCode());
            response.setPartnerBusinessName(agentResponse.getPartnerBusinessName());
            response.setSuperAgentId(agentResponse.getSuperAgentId());
            response.setSuperAgentUid(agentResponse.getSuperAgentUid());
            response.setSuperAgentCode(agentResponse.getSuperAgentCode());
            response.setSuperAgentBusinessName(agentResponse.getSuperAgentBusinessName());
            response.setUserId(agentResponse.getUserId());
            response.setUserUsername(agentResponse.getUserUsername());
            response.setUserEmail(agentResponse.getUserEmail());
            response.setVerificationRequestId(verificationRequestId);
            response.setMessage("Agent registration request submitted successfully. Verification request created.");

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseWrapper<>(true, 201, "Agent registration request submitted successfully", response));

        } catch (Exception e) {
            log.error("Error registering agent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseWrapper<>(false, 400, "Failed to submit agent registration request: " + e.getMessage(), null));
        }
    }


    // Response DTOs
    public static class AgentRegistrationResponse {
        // Agent fields
        private Long id;
        private String uid;
        private String agentCode;
        private String partnerAgentNumber;
        private String passName;
        // Note: passCode is sent via email for security reasons
        private String businessName;
        private String contactPerson;
        private String phoneNumber;
        private String businessEmail;
        private String businessAddress;
        private String taxId;
        private String licenseNumber;
        private String agentType;
        private String status;
        private String registrationDate;
        private String approvalDate;
        private String lastActivityDate;
        private String notes;
        private String createdAt;
        private String updatedAt;
        private Long partnerId;
        private String partnerUid;
        private String partnerCode;
        private String partnerBusinessName;
        private Long superAgentId;
        private String superAgentUid;
        private String superAgentCode;
        private String superAgentBusinessName;
        private Long userId;
        private String userUsername;
        private String userEmail;
        
        // Verification fields
        private String verificationRequestId;
        private String message;

        // Getters and setters for agent fields
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
        public String getAgentCode() { return agentCode; }
        public void setAgentCode(String agentCode) { this.agentCode = agentCode; }
        public String getPartnerAgentNumber() { return partnerAgentNumber; }
        public void setPartnerAgentNumber(String partnerAgentNumber) { this.partnerAgentNumber = partnerAgentNumber; }
        public String getPassName() { return passName; }
        public void setPassName(String passName) { this.passName = passName; }
        // Note: passCode getter/setter removed - sent via email for security
        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }
        public String getContactPerson() { return contactPerson; }
        public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getBusinessEmail() { return businessEmail; }
        public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }
        public String getBusinessAddress() { return businessAddress; }
        public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }
        public String getTaxId() { return taxId; }
        public void setTaxId(String taxId) { this.taxId = taxId; }
        public String getLicenseNumber() { return licenseNumber; }
        public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
        public String getAgentType() { return agentType; }
        public void setAgentType(String agentType) { this.agentType = agentType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
        public String getApprovalDate() { return approvalDate; }
        public void setApprovalDate(String approvalDate) { this.approvalDate = approvalDate; }
        public String getLastActivityDate() { return lastActivityDate; }
        public void setLastActivityDate(String lastActivityDate) { this.lastActivityDate = lastActivityDate; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
        public Long getPartnerId() { return partnerId; }
        public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
        public String getPartnerUid() { return partnerUid; }
        public void setPartnerUid(String partnerUid) { this.partnerUid = partnerUid; }
        public String getPartnerCode() { return partnerCode; }
        public void setPartnerCode(String partnerCode) { this.partnerCode = partnerCode; }
        public String getPartnerBusinessName() { return partnerBusinessName; }
        public void setPartnerBusinessName(String partnerBusinessName) { this.partnerBusinessName = partnerBusinessName; }
        public Long getSuperAgentId() { return superAgentId; }
        public void setSuperAgentId(Long superAgentId) { this.superAgentId = superAgentId; }
        public String getSuperAgentUid() { return superAgentUid; }
        public void setSuperAgentUid(String superAgentUid) { this.superAgentUid = superAgentUid; }
        public String getSuperAgentCode() { return superAgentCode; }
        public void setSuperAgentCode(String superAgentCode) { this.superAgentCode = superAgentCode; }
        public String getSuperAgentBusinessName() { return superAgentBusinessName; }
        public void setSuperAgentBusinessName(String superAgentBusinessName) { this.superAgentBusinessName = superAgentBusinessName; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserUsername() { return userUsername; }
        public void setUserUsername(String userUsername) { this.userUsername = userUsername; }
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        
        // Getters and setters for verification fields
        public String getVerificationRequestId() { return verificationRequestId; }
        public void setVerificationRequestId(String verificationRequestId) { this.verificationRequestId = verificationRequestId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

}
