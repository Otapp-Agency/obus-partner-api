package com.obuspartners.modules.bus_core_system.service;

import com.obuspartners.modules.bus_core_system.domain.dto.BusCoreSystemResponseDto;
import com.obuspartners.modules.bus_core_system.domain.dto.CreateBusCoreSystemRequestDto;
import com.obuspartners.modules.bus_core_system.domain.dto.UpdateBusCoreSystemRequestDto;

import java.util.List;

/**
 * Service interface for bus core system operations.
 * 
 * @author OBUS Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface BusCoreSystemService {
    
    /**
     * Create a new bus core system
     * 
     * @param request the create request DTO
     * @return the created system response DTO
     */
    BusCoreSystemResponseDto create(CreateBusCoreSystemRequestDto request);
    
    /**
     * Update an existing bus core system
     * 
     * @param uid the system UID
     * @param request the update request DTO
     * @return the updated system response DTO
     */
    BusCoreSystemResponseDto update(String uid, UpdateBusCoreSystemRequestDto request);
    
    /**
     * Get a bus core system by ID
     * 
     * @param id the system ID
     * @return the system response DTO
     * @throws ApiException if system not found
     */
    BusCoreSystemResponseDto getById(Long id);
    
    /**
     * Get a bus core system by UID
     * 
     * @param uid the system UID
     * @return the system response DTO
     * @throws ApiException if system not found
     */
    BusCoreSystemResponseDto getByUid(String uid);
    
    /**
     * Get a bus core system by code
     * 
     * @param code the system code
     * @return the system response DTO
     * @throws ApiException if system not found
     */
    BusCoreSystemResponseDto getByCode(String code);
    
    /**
     * Get a bus core system by name
     * 
     * @param name the system name
     * @return the system response DTO
     * @throws ApiException if system not found
     */
    BusCoreSystemResponseDto getByName(String name);
    
    /**
     * Get all bus core systems
     * 
     * @return list of all system response DTOs
     */
    List<BusCoreSystemResponseDto> getAll();
    
    /**
     * Delete a bus core system
     * 
     * @param uid the system UID
     * @throws ApiException if system not found
     */
    void delete(String uid);
    
    /**
     * Set a bus core system as default
     * 
     * @param uid the system UID
     * @return the updated system response DTO
     * @throws ApiException if system not found
     */
    BusCoreSystemResponseDto setAsDefault(String uid);
    
    /**
     * Get the default bus core system
     * 
     * @return the default system response DTO
     * @throws ApiException if no default system found
     */
    BusCoreSystemResponseDto getDefaultSystem();
    
}
