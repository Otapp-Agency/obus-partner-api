package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.AgentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Agent Request DTO
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAgentRequestDto {
    
    @NotNull(message = "Partner ID is required")
    private Long partnerId;
    
    @NotBlank(message = "Partner agent number is required")
    @Size(max = 50, message = "Partner agent number must not exceed 50 characters")
    private String partnerAgentNumber;
    
    @NotBlank(message = "Business name is required")
    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String businessName;
    
    @NotBlank(message = "Contact person is required")
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "MSISDN format is invalid")
    @Size(max = 20, message = "MSISDN must not exceed 20 characters")
    private String msisdn;
    
    @Email(message = "Business email must be valid")
    @Size(max = 100, message = "Business email must not exceed 100 characters")
    private String businessEmail;
    
    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;
    
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;
    
    @Size(max = 100, message = "License number must not exceed 100 characters")
    private String licenseNumber;
    
    @NotNull(message = "Agent type is required")
    private AgentType agentType;
    
    private Long superAgentId;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}