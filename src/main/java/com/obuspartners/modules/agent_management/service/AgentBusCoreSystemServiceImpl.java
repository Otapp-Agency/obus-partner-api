package com.obuspartners.modules.agent_management.service;

import com.obuspartners.modules.agent_management.domain.dto.*;
import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.domain.entity.AgentBusCoreSystem;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import com.obuspartners.modules.agent_management.repository.AgentBusCoreSystemRepository;
import com.obuspartners.modules.agent_management.repository.AgentRepository;
import com.obuspartners.modules.bus_core_system.repository.BusCoreSystemRepository;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.common.service.PasswordEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for AgentBusCoreSystem management
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AgentBusCoreSystemServiceImpl implements AgentBusCoreSystemService {

    private final AgentBusCoreSystemRepository agentBusCoreSystemRepository;
    private final PasswordEncryptionService passwordEncryptionService;
    private final AgentRepository agentRepository;
    private final BusCoreSystemRepository busCoreSystemRepository;

    @Override
    public AgentBusCoreSystemResponseDto assignAgentToBusCoreSystem(AssignAgentToBusCoreSystemRequest request) {
        log.info("Assigning agent {} to bus core system {}", request.getAgentId(), request.getBusCoreSystemId());
        
        // Load Agent and BusCoreSystem entities
        Agent agent = agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ApiException("Agent not found with ID: " + request.getAgentId(), org.springframework.http.HttpStatus.NOT_FOUND));
        BusCoreSystem busCoreSystem = busCoreSystemRepository.findById(request.getBusCoreSystemId())
                .orElseThrow(() -> new ApiException("Bus core system not found with ID: " + request.getBusCoreSystemId(), org.springframework.http.HttpStatus.NOT_FOUND));
        
        // Check if assignment already exists (combination of agent and bus core system should be unique)
        if (agentBusCoreSystemRepository.findByAgentAndBusCoreSystem(agent, busCoreSystem).isPresent()) {
            throw new ApiException("Agent is already assigned to this bus core system", org.springframework.http.HttpStatus.CONFLICT);
        }
        
        // Check if agent login name already exists for this bus core system
        if (agentBusCoreSystemRepository.existsByAgentLoginNameAndBusCoreSystem(request.getAgentLoginName(), busCoreSystem)) {
            throw new ApiException("Agent login name '" + request.getAgentLoginName() + "' already exists for this bus core system", org.springframework.http.HttpStatus.CONFLICT);
        }
        
        AgentBusCoreSystem agentBusCoreSystem = new AgentBusCoreSystem();
        agentBusCoreSystem.setAgent(agent);
        agentBusCoreSystem.setBusCoreSystem(busCoreSystem);
        agentBusCoreSystem.setAgentLoginName(request.getAgentLoginName());
        // Encrypt passwords before storing
        agentBusCoreSystem.setEncryptedPassword(passwordEncryptionService.encryptPassword(request.getPassword()));
        agentBusCoreSystem.setTxnUserName(request.getTxnUserName());
        if (request.getTxnPassword() != null) {
            agentBusCoreSystem.setEncryptedTxnPassword(passwordEncryptionService.encryptPassword(request.getTxnPassword()));
        }
        agentBusCoreSystem.setIsActive(request.getIsActive());
        agentBusCoreSystem.setIsPrimary(request.getIsPrimary());
        agentBusCoreSystem.setAgentStatusInBusCore(request.getAgentStatusInBusCore());
        agentBusCoreSystem.setBusCoreAgentId(request.getBusCoreAgentId());
        agentBusCoreSystem.setCreatedAt(LocalDateTime.now());
        
        AgentBusCoreSystem savedAgentBusCoreSystem = agentBusCoreSystemRepository.save(agentBusCoreSystem);
        log.info("Agent assigned to bus core system successfully with UID: {}", savedAgentBusCoreSystem.getUid());
        
        return convertToResponseDto(savedAgentBusCoreSystem);
    }

    @Override
    public Optional<AgentBusCoreSystem> findEntityByUid(String uid) {
        log.debug("Finding agent bus core system entity by UID: {}", uid);
        return agentBusCoreSystemRepository.findByUid(uid);
    }

    @Override
    public AgentBusCoreSystemResponseDto convertToResponseDto(AgentBusCoreSystem agentBusCoreSystem) {
        return AgentBusCoreSystemResponseDto.builder()
                .id(agentBusCoreSystem.getId())
                .uid(agentBusCoreSystem.getUid())
                .agentId(agentBusCoreSystem.getAgent().getId())
                .agentName(agentBusCoreSystem.getAgent().getContactPerson())
                .agentContactPerson(agentBusCoreSystem.getAgent().getContactPerson())
                .agentBusinessName(agentBusCoreSystem.getAgent().getBusinessName())
                .agentPhoneNumber(agentBusCoreSystem.getAgent().getPhoneNumber())
                .agentEmail(agentBusCoreSystem.getAgent().getBusinessEmail())
                .busCoreSystemId(agentBusCoreSystem.getBusCoreSystem().getId())
                .busCoreSystemName(agentBusCoreSystem.getBusCoreSystem().getName())
                .busCoreSystemCode(agentBusCoreSystem.getBusCoreSystem().getCode())
                .agentLoginName(agentBusCoreSystem.getAgentLoginName())
                // Decrypt passwords for development/testing purposes
                .password(passwordEncryptionService.decryptPassword(agentBusCoreSystem.getEncryptedPassword()))
                .txnUserName(agentBusCoreSystem.getTxnUserName())
                .txnPassword(agentBusCoreSystem.getEncryptedTxnPassword() != null ? 
                    passwordEncryptionService.decryptPassword(agentBusCoreSystem.getEncryptedTxnPassword()) : null)
                .agentStatusInBusCore(agentBusCoreSystem.getAgentStatusInBusCore())
                .isActive(agentBusCoreSystem.getIsActive())
                .isPrimary(agentBusCoreSystem.getIsPrimary())
                .busCoreAgentId(agentBusCoreSystem.getBusCoreAgentId())
                .createdAt(agentBusCoreSystem.getCreatedAt())
                .updatedAt(agentBusCoreSystem.getUpdatedAt())
                .createdBy(agentBusCoreSystem.getCreatedBy())
                .updatedBy(agentBusCoreSystem.getUpdatedBy())
                .lastAuthenticationDate(agentBusCoreSystem.getLastAuthenticationDate())
                .lastBookingDate(agentBusCoreSystem.getLastBookingDate())
                .build();
    }

    @Override
    public Optional<AgentBusCoreSystemResponseDto> findById(Long id) {
        log.debug("Finding agent bus core system by ID: {}", id);
        return agentBusCoreSystemRepository.findById(id)
                .map(this::convertToResponseDto);
    }

    @Override
    public Optional<AgentBusCoreSystemResponseDto> findByUid(String uid) {
        log.debug("Finding agent bus core system by UID: {}", uid);
        return agentBusCoreSystemRepository.findByUid(uid)
                .map(this::convertToResponseDto);
    }

    @Override
    public AgentBusCoreSystemResponseDto updateAgentBusCoreSystem(Long id, UpdateAgentBusCoreSystemRequest request) {
        log.info("Updating agent bus core system configuration {}", id);
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemRepository.findById(id)
                .orElseThrow(() -> new ApiException("Agent-Bus Core System relationship not found with ID: " + id, org.springframework.http.HttpStatus.NOT_FOUND));
        
        return updateAgentBusCoreSystemFields(agentBusCoreSystem, request);
    }

    @Override
    public AgentBusCoreSystemResponseDto updateAgentBusCoreSystemByUid(String uid, UpdateAgentBusCoreSystemRequest request) {
        log.info("Updating agent bus core system configuration {}", uid);
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent-Bus Core System relationship not found with UID: " + uid, org.springframework.http.HttpStatus.NOT_FOUND));
        
        return updateAgentBusCoreSystemFields(agentBusCoreSystem, request);
    }

    private AgentBusCoreSystemResponseDto updateAgentBusCoreSystemFields(AgentBusCoreSystem agentBusCoreSystem, UpdateAgentBusCoreSystemRequest request) {
        if (request.getAgentLoginName() != null) {
            agentBusCoreSystem.setAgentLoginName(request.getAgentLoginName());
        }
        if (request.getPassword() != null) {
            // Encrypt password before storing
            agentBusCoreSystem.setEncryptedPassword(passwordEncryptionService.encryptPassword(request.getPassword()));
        }
        if (request.getTxnUserName() != null) {
            agentBusCoreSystem.setTxnUserName(request.getTxnUserName());
        }
        if (request.getTxnPassword() != null) {
            // Encrypt transaction password before storing
            agentBusCoreSystem.setEncryptedTxnPassword(passwordEncryptionService.encryptPassword(request.getTxnPassword()));
        }
        if (request.getIsActive() != null) {
            agentBusCoreSystem.setIsActive(request.getIsActive());
        }
        if (request.getIsPrimary() != null) {
            agentBusCoreSystem.setIsPrimary(request.getIsPrimary());
        }
        if (request.getAgentStatusInBusCore() != null) {
            agentBusCoreSystem.setAgentStatusInBusCore(request.getAgentStatusInBusCore());
        }
        if (request.getBusCoreAgentId() != null) {
            agentBusCoreSystem.setBusCoreAgentId(request.getBusCoreAgentId());
        }
        
        agentBusCoreSystem.setUpdatedAt(LocalDateTime.now());
        
        AgentBusCoreSystem updatedAgentBusCoreSystem = agentBusCoreSystemRepository.save(agentBusCoreSystem);
        log.info("Agent bus core system configuration updated successfully");
        
        return convertToResponseDto(updatedAgentBusCoreSystem);
    }

    @Override
    public Optional<AgentBusCoreSystem> getAgentBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem) {
        log.debug("Getting agent bus core system for agent {} and bus core system {}", agent.getId(), busCoreSystem.getId());
        return agentBusCoreSystemRepository.findByAgentAndBusCoreSystem(agent, busCoreSystem);
    }

    @Override
    public List<AgentBusCoreSystemResponseDto> getBusCoreSystemsByAgent(Agent agent) {
        log.debug("Getting bus core systems for agent {}", agent.getId());
        return agentBusCoreSystemRepository.findByAgent(agent)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    public List<AgentBusCoreSystemResponseDto> getBusCoreSystemsByAgentId(Long agentId) {
        log.debug("Getting bus core systems for agent ID {}", agentId);
        return agentBusCoreSystemRepository.findByAgentId(agentId)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    public List<AgentBusCoreSystemResponseDto> getBusCoreSystemsByAgentUid(String agentUid) {
        log.debug("Getting bus core systems for agent UID {}", agentUid);
        return agentBusCoreSystemRepository.findByAgentUid(agentUid)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    public List<AgentBusCoreSystemResponseDto> getAgentsByBusCoreSystem(BusCoreSystem busCoreSystem) {
        log.debug("Getting agents for bus core system {}", busCoreSystem.getId());
        return agentBusCoreSystemRepository.findByBusCoreSystem(busCoreSystem)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    public List<AgentBusCoreSystemResponseDto> getAgentsByBusCoreSystemId(Long busCoreSystemId) {
        log.debug("Getting agents for bus core system ID {}", busCoreSystemId);
        return agentBusCoreSystemRepository.findByBusCoreSystemId(busCoreSystemId)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    public List<AgentBusCoreSystemResponseDto> getActiveBusCoreSystemsByAgent(Agent agent) {
        log.debug("Getting active bus core systems for agent {}", agent.getId());
        return agentBusCoreSystemRepository.findByAgentAndIsActiveTrue(agent)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    public Optional<AgentBusCoreSystemResponseDto> getPrimaryBusCoreSystemByAgent(Agent agent) {
        log.debug("Getting primary bus core system for agent {}", agent.getId());
        return agentBusCoreSystemRepository.findByAgentAndIsPrimaryTrue(agent)
                .map(this::convertToResponseDto);
    }

    @Override
    public AgentBusCoreSystemResponseDto setPrimaryBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem) {
        log.info("Setting primary bus core system for agent {}", agent.getId());
        
        // First, remove primary status from all other bus core systems for this agent
        Optional<AgentBusCoreSystem> existingPrimarySystemOpt = agentBusCoreSystemRepository.findByAgentAndIsPrimaryTrue(agent);
        if (existingPrimarySystemOpt.isPresent()) {
            AgentBusCoreSystem existingPrimarySystem = existingPrimarySystemOpt.get();
            existingPrimarySystem.setIsPrimary(false);
            agentBusCoreSystemRepository.save(existingPrimarySystem);
        }
        
        // Set the new primary system
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemRepository.findByAgentAndBusCoreSystem(agent, busCoreSystem)
                .orElseThrow(() -> new ApiException("Agent-Bus Core System relationship not found", org.springframework.http.HttpStatus.NOT_FOUND));
        
        agentBusCoreSystem.setIsPrimary(true);
        agentBusCoreSystem.setUpdatedAt(LocalDateTime.now());
        
        AgentBusCoreSystem updatedAgentBusCoreSystem = agentBusCoreSystemRepository.save(agentBusCoreSystem);
        log.info("Primary bus core system set successfully");
        
        return convertToResponseDto(updatedAgentBusCoreSystem);
    }

    @Override
    public AgentBusCoreSystemResponseDto setAgentActiveStatus(Long id, boolean isActive) {
        log.info("Setting agent bus core system {} active status to {}", id, isActive);
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemRepository.findById(id)
                .orElseThrow(() -> new ApiException("Agent-Bus Core System relationship not found with ID: " + id, org.springframework.http.HttpStatus.NOT_FOUND));
        
        agentBusCoreSystem.setIsActive(isActive);
        agentBusCoreSystem.setUpdatedAt(LocalDateTime.now());
        
        AgentBusCoreSystem updatedAgentBusCoreSystem = agentBusCoreSystemRepository.save(agentBusCoreSystem);
        log.info("Agent active status updated successfully");
        
        return convertToResponseDto(updatedAgentBusCoreSystem);
    }

    @Override
    public AgentBusCoreSystemResponseDto setAgentActiveStatusByUid(String uid, boolean isActive) {
        log.info("Setting agent bus core system {} active status to {}", uid, isActive);
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException("Agent-Bus Core System relationship not found with UID: " + uid, org.springframework.http.HttpStatus.NOT_FOUND));
        
        agentBusCoreSystem.setIsActive(isActive);
        agentBusCoreSystem.setUpdatedAt(LocalDateTime.now());
        
        AgentBusCoreSystem updatedAgentBusCoreSystem = agentBusCoreSystemRepository.save(agentBusCoreSystem);
        log.info("Agent active status updated successfully");
        
        return convertToResponseDto(updatedAgentBusCoreSystem);
    }

    @Override
    public AgentBusCoreSystemResponseDto updateAgentCredentials(Long id, String agentLoginName, String password, String txnPassword) {
        log.info("Updating agent credentials for agent bus core system {}", id);
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemRepository.findById(id)
                .orElseThrow(() -> new ApiException("Agent-Bus Core System relationship not found with ID: " + id, org.springframework.http.HttpStatus.NOT_FOUND));
        
        if (agentLoginName != null) {
            agentBusCoreSystem.setAgentLoginName(agentLoginName);
        }
        if (password != null) {
            // Encrypt password before storing
            agentBusCoreSystem.setEncryptedPassword(passwordEncryptionService.encryptPassword(password));
        }
        if (txnPassword != null) {
            // Encrypt transaction password before storing
            agentBusCoreSystem.setEncryptedTxnPassword(passwordEncryptionService.encryptPassword(txnPassword));
        }
        
        agentBusCoreSystem.setUpdatedAt(LocalDateTime.now());
        
        AgentBusCoreSystem updatedAgentBusCoreSystem = agentBusCoreSystemRepository.save(agentBusCoreSystem);
        log.info("Agent credentials updated successfully");
        
        return convertToResponseDto(updatedAgentBusCoreSystem);
    }

    @Override
    public boolean authenticateAgentForBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem, String agentLoginName, String password) {
        log.debug("Authenticating agent {} for bus core system {}", agent.getId(), busCoreSystem.getId());
        
        Optional<AgentBusCoreSystem> agentBusCoreSystemOpt = agentBusCoreSystemRepository.findByAgentAndBusCoreSystem(agent, busCoreSystem);
        
        if (agentBusCoreSystemOpt.isEmpty()) {
            return false;
        }
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemOpt.get();
        
        // Decrypt stored password for comparison
        String decryptedPassword = passwordEncryptionService.decryptPassword(agentBusCoreSystem.getEncryptedPassword());
        
        boolean isValid = agentBusCoreSystem.isCredentialsValid() && 
                         agentLoginName.equals(agentBusCoreSystem.getAgentLoginName()) &&
                         password.equals(decryptedPassword);
        
        if (isValid) {
            agentBusCoreSystem.setLastAuthenticationDate(LocalDateTime.now());
            agentBusCoreSystemRepository.save(agentBusCoreSystem);
        }
        
        return isValid;
    }

    @Override
    public Optional<AgentBusCoreSystem> getAgentCredentialsForBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem) {
        log.debug("Getting agent credentials for agent {} and bus core system {}", agent.getId(), busCoreSystem.getId());
        return agentBusCoreSystemRepository.findByAgentAndBusCoreSystem(agent, busCoreSystem);
    }

    @Override
    public Optional<DecryptedAgentCredentials> getDecryptedAgentCredentialsForBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem) {
        log.debug("Getting decrypted agent credentials for agent {} and bus core system {}", agent.getId(), busCoreSystem.getId());
        
        Optional<AgentBusCoreSystem> agentBusCoreSystemOpt = agentBusCoreSystemRepository.findByAgentAndBusCoreSystem(agent, busCoreSystem);
        
        if (agentBusCoreSystemOpt.isEmpty()) {
            return Optional.empty();
        }
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemOpt.get();
        
        // Decrypt passwords for use by the bus core system
        String decryptedPassword = passwordEncryptionService.decryptPassword(agentBusCoreSystem.getEncryptedPassword());
        String decryptedTxnPassword = null;
        if (agentBusCoreSystem.getEncryptedTxnPassword() != null) {
            decryptedTxnPassword = passwordEncryptionService.decryptPassword(agentBusCoreSystem.getEncryptedTxnPassword());
        }
        
        DecryptedAgentCredentials credentials = DecryptedAgentCredentials.builder()
                .agentLoginName(agentBusCoreSystem.getAgentLoginName())
                .password(decryptedPassword)
                .txnUserName(agentBusCoreSystem.getTxnUserName())
                .txnPassword(decryptedTxnPassword)
                .agentStatusInBusCore(agentBusCoreSystem.getAgentStatusInBusCore())
                .busCoreAgentId(agentBusCoreSystem.getBusCoreAgentId())
                .isActive(agentBusCoreSystem.getIsActive())
                .isPrimary(agentBusCoreSystem.getIsPrimary())
                .build();
        
        return Optional.of(credentials);
    }

    @Override
    public void removeAgentFromBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem) {
        log.info("Removing agent {} from bus core system {}", agent.getId(), busCoreSystem.getId());
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemRepository.findByAgentAndBusCoreSystem(agent, busCoreSystem)
                .orElseThrow(() -> new ApiException("Agent-Bus Core System relationship not found", org.springframework.http.HttpStatus.NOT_FOUND));
        
        agentBusCoreSystemRepository.delete(agentBusCoreSystem);
        log.info("Agent removed from bus core system successfully");
    }

    @Override
    public boolean canAgentPerformOperation(Agent agent, BusCoreSystem busCoreSystem, String operation) {
        log.debug("Checking if agent {} can perform operation {} on bus core system {}", agent.getId(), operation, busCoreSystem.getId());
        
        Optional<AgentBusCoreSystem> agentBusCoreSystemOpt = agentBusCoreSystemRepository.findByAgentAndBusCoreSystem(agent, busCoreSystem);
        
        if (agentBusCoreSystemOpt.isEmpty() || !agentBusCoreSystemOpt.get().getIsActive()) {
            return false;
        }
        
        // For now, return true if agent is active
        // In a real implementation, this would check specific permissions based on the operation
        return true;
    }

    @Override
    public AgentBusCoreSystemResponseDto getAgentPermissions(Agent agent, BusCoreSystem busCoreSystem) {
        log.debug("Getting agent permissions for agent {} and bus core system {}", agent.getId(), busCoreSystem.getId());
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemRepository.findByAgentAndBusCoreSystem(agent, busCoreSystem)
                .orElseThrow(() -> new ApiException("Agent-Bus Core System relationship not found", org.springframework.http.HttpStatus.NOT_FOUND));
        
        return AgentBusCoreSystemResponseDto.builder()
                .id(agentBusCoreSystem.getId())
                .uid(agentBusCoreSystem.getUid())
                .agentId(agent.getId())
                .agentName(agent.getContactPerson())
                .agentContactPerson(agent.getContactPerson())
                .agentBusinessName(agent.getBusinessName())
                .agentPhoneNumber(agent.getPhoneNumber())
                .agentEmail(agent.getBusinessEmail())
                .busCoreSystemId(busCoreSystem.getId())
                .busCoreSystemName(busCoreSystem.getName())
                .busCoreSystemCode(busCoreSystem.getCode())
                .agentLoginName(agentBusCoreSystem.getAgentLoginName())
                .txnUserName(agentBusCoreSystem.getTxnUserName())
                .agentStatusInBusCore(agentBusCoreSystem.getAgentStatusInBusCore())
                .isActive(agentBusCoreSystem.getIsActive())
                .isPrimary(agentBusCoreSystem.getIsPrimary())
                .busCoreAgentId(agentBusCoreSystem.getBusCoreAgentId())
                .createdAt(agentBusCoreSystem.getCreatedAt())
                .updatedAt(agentBusCoreSystem.getUpdatedAt())
                .createdBy(agentBusCoreSystem.getCreatedBy())
                .updatedBy(agentBusCoreSystem.getUpdatedBy())
                .lastAuthenticationDate(agentBusCoreSystem.getLastAuthenticationDate())
                .lastBookingDate(agentBusCoreSystem.getLastBookingDate())
                .build();
    }

    @Override
    public AgentBusCoreSystemResponseDto updateAgentPermissions(Long id, UpdateAgentBusCoreSystemRequest request) {
        log.info("Updating agent permissions for agent bus core system {}", id);
        
        AgentBusCoreSystem agentBusCoreSystem = agentBusCoreSystemRepository.findById(id)
                .orElseThrow(() -> new ApiException("Agent-Bus Core System relationship not found with ID: " + id, org.springframework.http.HttpStatus.NOT_FOUND));
        
        return updateAgentBusCoreSystemFields(agentBusCoreSystem, request);
    }

    @Override
    public List<AgentBusCoreSystem> findAllForKeyRotation() {
        log.debug("Finding all AgentBusCoreSystem records for key rotation");
        return agentBusCoreSystemRepository.findAll();
    }

    @Override
    public AgentBusCoreSystem save(AgentBusCoreSystem agentBusCoreSystem) {
        log.debug("Saving AgentBusCoreSystem with ID: {}", agentBusCoreSystem.getId());
        return agentBusCoreSystemRepository.save(agentBusCoreSystem);
    }
}
