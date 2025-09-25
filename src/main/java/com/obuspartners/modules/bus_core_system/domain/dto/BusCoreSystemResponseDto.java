package com.obuspartners.modules.bus_core_system.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for bus core system response data.
 * 
 * @author OBUS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class BusCoreSystemResponseDto {
    
    private Long id;
    private String uid;
    private String code;
    private String name;
    private String providerName;
    private String baseUrl;
    private String description;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
