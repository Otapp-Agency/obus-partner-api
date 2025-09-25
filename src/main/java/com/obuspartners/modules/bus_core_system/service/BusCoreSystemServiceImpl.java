package com.obuspartners.modules.bus_core_system.service;

import com.obuspartners.modules.bus_core_system.domain.dto.BusCoreSystemResponseDto;
import com.obuspartners.modules.bus_core_system.domain.dto.CreateBusCoreSystemRequestDto;
import com.obuspartners.modules.bus_core_system.domain.dto.UpdateBusCoreSystemRequestDto;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import com.obuspartners.modules.bus_core_system.repository.BusCoreSystemRepository;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for bus core system operations.
 * 
 * @author OBUS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusCoreSystemServiceImpl implements BusCoreSystemService {
    
    private final BusCoreSystemRepository busCoreSystemRepository;
    private final UserRepository userRepository;
    
    @Override
    public BusCoreSystemResponseDto create(CreateBusCoreSystemRequestDto request) {
        log.info("Creating new bus core system: {}", request.getName());
        
        // Check if code already exists
        if (busCoreSystemRepository.existsByCode(request.getCode())) {
            throw new ApiException("System code already exists: " + request.getCode(), HttpStatus.CONFLICT);
        }
        
        // Check if name already exists
        if (busCoreSystemRepository.existsByName(request.getName())) {
            throw new ApiException("System name already exists: " + request.getName(), HttpStatus.CONFLICT);
        }
        
        // Create new system
        BusCoreSystem system = new BusCoreSystem();
        system.setCode(request.getCode());
        system.setName(request.getName());
        system.setProviderName(request.getProviderName());
        system.setBaseUrl(request.getBaseUrl());
        system.setDescription(request.getDescription());
        system.setIsDefault(request.getIsDefault());
        system.setIsDeleted(false);
        
        // Set audit fields
        User currentUser = getCurrentUser();
        system.setCreatedBy(currentUser);
        system.setUpdatedBy(currentUser);
        
        // If this is set as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetDefaultSystems();
        }
        
        // Save system
        system = busCoreSystemRepository.save(system);
        
        log.info("Successfully created bus core system: {} with UID: {}", system.getName(), system.getUid());
        
        return mapToResponseDto(system);
    }
    
    @Override
    public BusCoreSystemResponseDto update(String uid, UpdateBusCoreSystemRequestDto request) {
        log.info("Updating bus core system with UID: {}", uid);
        
        BusCoreSystem system = busCoreSystemRepository.findByUidAndIsDeletedFalse(uid)
            .orElseThrow(() -> new ApiException("Bus core system not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Check if code already exists (excluding current system)
        if (busCoreSystemRepository.existsByCodeAndIdNot(request.getCode(), system.getId())) {
            throw new ApiException("System code already exists: " + request.getCode(), HttpStatus.CONFLICT);
        }
        
        // Check if name already exists (excluding current system)
        if (busCoreSystemRepository.existsByNameAndIdNot(request.getName(), system.getId())) {
            throw new ApiException("System name already exists: " + request.getName(), HttpStatus.CONFLICT);
        }
        
        // Update system
        system.setCode(request.getCode());
        system.setName(request.getName());
        system.setProviderName(request.getProviderName());
        system.setBaseUrl(request.getBaseUrl());
        system.setDescription(request.getDescription());
        system.setIsDefault(request.getIsDefault());
        
        // Set updated by user
        User currentUser = getCurrentUser();
        system.setUpdatedBy(currentUser);
        
        // If this is set as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetDefaultSystems();
        }
        
        // Save system
        system = busCoreSystemRepository.save(system);
        
        log.info("Successfully updated bus core system: {} with UID: {}", system.getName(), system.getUid());
        
        return mapToResponseDto(system);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BusCoreSystemResponseDto getById(Long id) {
        BusCoreSystem system = busCoreSystemRepository.findById(id)
            .filter(s -> !s.getIsDeleted())
            .orElseThrow(() -> new ApiException("Bus core system not found with ID: " + id, HttpStatus.NOT_FOUND));
        
        return mapToResponseDto(system);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BusCoreSystemResponseDto getByUid(String uid) {
        BusCoreSystem system = busCoreSystemRepository.findByUidAndIsDeletedFalse(uid)
            .orElseThrow(() -> new ApiException("Bus core system not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        return mapToResponseDto(system);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BusCoreSystemResponseDto getByCode(String code) {
        BusCoreSystem system = busCoreSystemRepository.findByCodeAndIsDeletedFalse(code)
            .orElseThrow(() -> new ApiException("Bus core system not found with code: " + code, HttpStatus.NOT_FOUND));
        
        return mapToResponseDto(system);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BusCoreSystemResponseDto getByName(String name) {
        BusCoreSystem system = busCoreSystemRepository.findByNameAndIsDeletedFalse(name)
            .orElseThrow(() -> new ApiException("Bus core system not found with name: " + name, HttpStatus.NOT_FOUND));
        
        return mapToResponseDto(system);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BusCoreSystemResponseDto> getAll() {
        List<BusCoreSystem> systems = busCoreSystemRepository.findByIsDeletedFalseOrderByNameAsc();
        
        return systems.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(String uid) {
        log.info("Soft deleting bus core system with UID: {}", uid);
        
        BusCoreSystem system = busCoreSystemRepository.findByUidAndIsDeletedFalse(uid)
            .orElseThrow(() -> new ApiException("Bus core system not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Check if this is the default system
        if (Boolean.TRUE.equals(system.getIsDefault())) {
            throw new ApiException("Cannot delete default system. Set another system as default first.", HttpStatus.BAD_REQUEST);
        }
        
        // Soft delete
        system.setIsDeleted(true);
        User currentUser = getCurrentUser();
        system.setUpdatedBy(currentUser);
        busCoreSystemRepository.save(system);
        
        log.info("Successfully soft deleted bus core system: {} with UID: {}", system.getName(), system.getUid());
    }
    
    @Override
    public BusCoreSystemResponseDto setAsDefault(String uid) {
        log.info("Setting bus core system as default with UID: {}", uid);
        
        BusCoreSystem system = busCoreSystemRepository.findByUidAndIsDeletedFalse(uid)
            .orElseThrow(() -> new ApiException("Bus core system not found with UID: " + uid, HttpStatus.NOT_FOUND));
        
        // Unset other defaults
        unsetDefaultSystems();
        
        // Set this as default
        system.setIsDefault(true);
        User currentUser = getCurrentUser();
        system.setUpdatedBy(currentUser);
        system = busCoreSystemRepository.save(system);
        
        log.info("Successfully set bus core system as default: {} with UID: {}", system.getName(), system.getUid());
        
        return mapToResponseDto(system);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BusCoreSystemResponseDto getDefaultSystem() {
        BusCoreSystem system = busCoreSystemRepository.findByIsDefaultTrueAndIsDeletedFalse()
            .orElseThrow(() -> new ApiException("No default bus core system found", HttpStatus.NOT_FOUND));
        
        return mapToResponseDto(system);
    }
    
    // Private helper methods
    
    private void unsetDefaultSystems() {
        List<BusCoreSystem> defaultSystems = busCoreSystemRepository.findAll()
            .stream()
            .filter(s -> Boolean.TRUE.equals(s.getIsDefault()) && !s.getIsDeleted())
            .collect(Collectors.toList());
        
        User currentUser = getCurrentUser();
        for (BusCoreSystem system : defaultSystems) {
            system.setIsDefault(false);
            system.setUpdatedBy(currentUser);
            busCoreSystemRepository.save(system);
        }
    }
    
    private BusCoreSystemResponseDto mapToResponseDto(BusCoreSystem system) {
        BusCoreSystemResponseDto dto = new BusCoreSystemResponseDto();
        dto.setId(system.getId());
        dto.setUid(system.getUid());
        dto.setCode(system.getCode());
        dto.setName(system.getName());
        dto.setProviderName(system.getProviderName());
        dto.setBaseUrl(system.getBaseUrl());
        dto.setDescription(system.getDescription());
        dto.setIsDefault(system.getIsDefault());
        dto.setCreatedAt(system.getCreatedAt());
        dto.setUpdatedAt(system.getUpdatedAt());
        dto.setCreatedBy(system.getCreatedBy() != null ? 
            system.getCreatedBy().getDisplayName() : null);
        dto.setUpdatedBy(system.getUpdatedBy() != null ? 
            system.getUpdatedBy().getDisplayName() : null);
        
        return dto;
    }
    
    /**
     * Get the current authenticated user from security context
     * 
     * @return the current user
     * @throws ApiException if no user is authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException("No authenticated user found", HttpStatus.UNAUTHORIZED);
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found: " + username, HttpStatus.NOT_FOUND));
    }
}
