package com.obuspartners.modules.agent_management.domain.dto;

import com.obuspartners.modules.agent_management.domain.enums.GroupAgentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new GroupAgent
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupAgentRequestDto {

    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "External system identifier is required")
    @Size(max = 100, message = "External system identifier must not exceed 100 characters")
    private String externalSystemIdentifier;

    private GroupAgentType type;

    // Contact information
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @Email(message = "Contact email must be valid")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    // Business information
    @Size(max = 200, message = "Business name must not exceed 200 characters")
    private String businessName;

    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;

    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @Size(max = 50, message = "License number must not exceed 50 characters")
    private String licenseNumber;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
