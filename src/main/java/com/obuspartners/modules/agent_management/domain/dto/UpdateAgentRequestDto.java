package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.AgentType;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing agent
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgentRequestDto {

    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String businessName;

    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,20}$", message = "Phone number format is invalid")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Email(message = "Business email must be valid")
    @Size(max = 100, message = "Business email must not exceed 100 characters")
    private String businessEmail;

    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;

    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @Size(max = 50, message = "License number must not exceed 50 characters")
    private String licenseNumber;

    private AgentType agentType;

    private AgentStatus status;

    private Long superAgentId;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
