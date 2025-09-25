package com.obuspartners.modules.bus_core_system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for updating an existing bus core system.
 * 
 * @author OBUS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class UpdateBusCoreSystemRequestDto {
    
    @NotBlank(message = "System code is required")
    @Size(max = 20, message = "System code must not exceed 20 characters")
    private String code;
    
    @NotBlank(message = "System name is required")
    @Size(max = 100, message = "System name must not exceed 100 characters")
    private String name;
    
    @NotBlank(message = "Provider name is required")
    @Size(max = 200, message = "Provider name must not exceed 200 characters")
    private String providerName;
    
    @NotBlank(message = "Base URL is required")
    @Size(max = 500, message = "Base URL must not exceed 500 characters")
    private String baseUrl;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private Boolean isDefault = false;
}
