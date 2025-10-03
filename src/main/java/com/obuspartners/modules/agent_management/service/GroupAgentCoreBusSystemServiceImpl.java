package com.obuspartners.modules.agent_management.service;

import com.obuspartners.modules.agent_management.domain.entity.GroupAgent;
import com.obuspartners.modules.agent_management.domain.entity.GroupAgentCoreBusSystem;
import com.obuspartners.modules.agent_management.repository.GroupAgentCoreBusSystemRepository;
import com.obuspartners.modules.agent_management.repository.GroupAgentRepository;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import com.obuspartners.modules.bus_core_system.repository.BusCoreSystemRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for GroupAgentCoreBusSystem management operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupAgentCoreBusSystemServiceImpl implements GroupAgentCoreBusSystemService {

    private final GroupAgentCoreBusSystemRepository groupAgentCoreBusSystemRepository;
    private final GroupAgentRepository groupAgentRepository;
    private final BusCoreSystemRepository busCoreSystemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public GroupAgentCoreBusSystem assignGroupAgentToBusCoreSystem(
            GroupAgent groupAgent, 
            BusCoreSystem busCoreSystem,
            String externalAgentIdentifier,
            String username,
            String password,
            String apiKey,
            String apiSecret,
            boolean isPrimary) {
        
        log.info("Assigning group agent {} to bus core system {}", groupAgent.getName(), busCoreSystem.getName());
        
        // Load GroupAgent and BusCoreSystem entities to ensure they exist
        GroupAgent loadedGroupAgent = groupAgentRepository.findById(groupAgent.getId())
                .orElseThrow(() -> new RuntimeException("Group agent not found with ID: " + groupAgent.getId()));
        BusCoreSystem loadedBusCoreSystem = busCoreSystemRepository.findById(busCoreSystem.getId())
                .orElseThrow(() -> new RuntimeException("Bus core system not found with ID: " + busCoreSystem.getId()));
        
        // Check if assignment already exists
        if (groupAgentCoreBusSystemRepository.existsByGroupAgentAndBusCoreSystem(loadedGroupAgent, loadedBusCoreSystem)) {
            throw new RuntimeException("Group agent is already assigned to this bus core system");
        }
        
        // Check if external agent identifier already exists for this bus core system
        if (groupAgentCoreBusSystemRepository.existsByBusCoreSystemAndExternalAgentIdentifier(loadedBusCoreSystem, externalAgentIdentifier)) {
            throw new RuntimeException("External agent identifier '" + externalAgentIdentifier + "' already exists for this bus core system");
        }
        
        // If this is being set as primary, unset any existing primary for this group agent
        if (isPrimary) {
            Optional<GroupAgentCoreBusSystem> existingPrimary = groupAgentCoreBusSystemRepository.findByGroupAgentAndIsPrimaryTrue(loadedGroupAgent);
            if (existingPrimary.isPresent()) {
                existingPrimary.get().setPrimary(false);
                groupAgentCoreBusSystemRepository.save(existingPrimary.get());
            }
        }
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = new GroupAgentCoreBusSystem();
        groupAgentCoreBusSystem.setGroupAgent(loadedGroupAgent);
        groupAgentCoreBusSystem.setBusCoreSystem(loadedBusCoreSystem);
        groupAgentCoreBusSystem.setExternalAgentIdentifier(externalAgentIdentifier);
        groupAgentCoreBusSystem.setUsername(username);
        groupAgentCoreBusSystem.setPassword(passwordEncoder.encode(password));
        groupAgentCoreBusSystem.setApiKey(apiKey != null ? passwordEncoder.encode(apiKey) : null);
        groupAgentCoreBusSystem.setApiSecret(apiSecret != null ? passwordEncoder.encode(apiSecret) : null);
        groupAgentCoreBusSystem.setIsActive(true);
        groupAgentCoreBusSystem.setIsPrimary(isPrimary);
        groupAgentCoreBusSystem.setExternalSystemStatus("ACTIVE");
        
        GroupAgentCoreBusSystem savedGroupAgentCoreBusSystem = groupAgentCoreBusSystemRepository.save(groupAgentCoreBusSystem);
        
        // Add to group agent's collection
        loadedGroupAgent.addCoreBusSystem(savedGroupAgentCoreBusSystem);
        groupAgentRepository.save(loadedGroupAgent);
        
        log.info("Successfully assigned group agent to bus core system with ID: {}", savedGroupAgentCoreBusSystem.getId());
        
        return savedGroupAgentCoreBusSystem;
    }

    @Override
    public GroupAgentCoreBusSystem updateGroupAgentBusCoreSystemCredentials(GroupAgentCoreBusSystem groupAgentCoreBusSystem) {
        log.info("Updating credentials for group agent bus core system: {}", groupAgentCoreBusSystem.getId());
        
        // Note: Passwords should already be encrypted when passed to this method
        // This method is for updating existing credentials
        
        GroupAgentCoreBusSystem updatedGroupAgentCoreBusSystem = groupAgentCoreBusSystemRepository.save(groupAgentCoreBusSystem);
        log.info("Successfully updated credentials for group agent bus core system with ID: {}", updatedGroupAgentCoreBusSystem.getId());
        
        return updatedGroupAgentCoreBusSystem;
    }

    @Override
    public GroupAgentCoreBusSystem updateCredentialsById(Long id, String username, String password, String apiKey, String apiSecret) {
        log.info("Updating credentials for group agent bus core system ID: {}", id);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group agent bus core system not found with ID: " + id));
        
        if (username != null) {
            groupAgentCoreBusSystem.setUsername(username);
        }
        if (password != null) {
            groupAgentCoreBusSystem.setPassword(passwordEncoder.encode(password));
        }
        if (apiKey != null) {
            groupAgentCoreBusSystem.setApiKey(passwordEncoder.encode(apiKey));
        }
        if (apiSecret != null) {
            groupAgentCoreBusSystem.setApiSecret(passwordEncoder.encode(apiSecret));
        }
        
        return groupAgentCoreBusSystemRepository.save(groupAgentCoreBusSystem);
    }

    @Override
    public GroupAgentCoreBusSystem updateCredentialsByUid(String uid, String username, String password, String apiKey, String apiSecret) {
        log.info("Updating credentials for group agent bus core system UID: {}", uid);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Group agent bus core system not found with UID: " + uid));
        
        if (username != null) {
            groupAgentCoreBusSystem.setUsername(username);
        }
        if (password != null) {
            groupAgentCoreBusSystem.setPassword(passwordEncoder.encode(password));
        }
        if (apiKey != null) {
            groupAgentCoreBusSystem.setApiKey(passwordEncoder.encode(apiKey));
        }
        if (apiSecret != null) {
            groupAgentCoreBusSystem.setApiSecret(passwordEncoder.encode(apiSecret));
        }
        
        return groupAgentCoreBusSystemRepository.save(groupAgentCoreBusSystem);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupAgentCoreBusSystem> getGroupAgentCoreBusSystemById(Long id) {
        log.debug("Finding group agent bus core system by ID: {}", id);
        return groupAgentCoreBusSystemRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupAgentCoreBusSystem> getGroupAgentCoreBusSystemByUid(String uid) {
        log.debug("Finding group agent bus core system by UID: {}", uid);
        return groupAgentCoreBusSystemRepository.findByUid(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupAgentCoreBusSystem> getByGroupAgentAndBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem) {
        log.debug("Finding group agent bus core system by group agent {} and bus core system {}", groupAgent.getName(), busCoreSystem.getName());
        return groupAgentCoreBusSystemRepository.findByGroupAgentAndBusCoreSystem(groupAgent, busCoreSystem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgentCoreBusSystem> getBusCoreSystemsByGroupAgent(GroupAgent groupAgent) {
        log.debug("Finding all bus core systems for group agent: {}", groupAgent.getName());
        return groupAgentCoreBusSystemRepository.findByGroupAgent(groupAgent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupAgentCoreBusSystem> getBusCoreSystemsByGroupAgent(GroupAgent groupAgent, Pageable pageable) {
        log.debug("Finding bus core systems for group agent {} with pagination", groupAgent.getName());
        return groupAgentCoreBusSystemRepository.findByGroupAgent(groupAgent, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgentCoreBusSystem> getActiveBusCoreSystemsByGroupAgent(GroupAgent groupAgent) {
        log.debug("Finding active bus core systems for group agent: {}", groupAgent.getName());
        return groupAgentCoreBusSystemRepository.findByGroupAgentAndIsActiveTrue(groupAgent);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupAgentCoreBusSystem> getPrimaryBusCoreSystemByGroupAgent(GroupAgent groupAgent) {
        log.debug("Finding primary bus core system for group agent: {}", groupAgent.getName());
        return groupAgentCoreBusSystemRepository.findByGroupAgentAndIsPrimaryTrue(groupAgent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgentCoreBusSystem> getByBusCoreSystem(BusCoreSystem busCoreSystem) {
        log.debug("Finding group agent bus core systems by bus core system: {}", busCoreSystem.getName());
        return groupAgentCoreBusSystemRepository.findByBusCoreSystem(busCoreSystem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgentCoreBusSystem> getActiveByBusCoreSystem(BusCoreSystem busCoreSystem) {
        log.debug("Finding active group agent bus core systems by bus core system: {}", busCoreSystem.getName());
        return groupAgentCoreBusSystemRepository.findByBusCoreSystemAndIsActiveTrue(busCoreSystem);
    }

    @Override
    public GroupAgentCoreBusSystem activateGroupAgentForBusCoreSystem(GroupAgentCoreBusSystem groupAgentCoreBusSystem) {
        log.info("Activating group agent for bus core system: {}", groupAgentCoreBusSystem.getId());
        groupAgentCoreBusSystem.activate();
        return groupAgentCoreBusSystemRepository.save(groupAgentCoreBusSystem);
    }

    @Override
    public GroupAgentCoreBusSystem deactivateGroupAgentForBusCoreSystem(GroupAgentCoreBusSystem groupAgentCoreBusSystem) {
        log.info("Deactivating group agent for bus core system: {}", groupAgentCoreBusSystem.getId());
        groupAgentCoreBusSystem.deactivate();
        return groupAgentCoreBusSystemRepository.save(groupAgentCoreBusSystem);
    }

    @Override
    public GroupAgentCoreBusSystem setPrimaryBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem) {
        log.info("Setting primary bus core system for group agent: {}", groupAgent.getName());
        
        // Find the GroupAgentCoreBusSystem relationship
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemRepository.findByGroupAgentAndBusCoreSystem(groupAgent, busCoreSystem)
                .orElseThrow(() -> new RuntimeException("Group agent is not assigned to this bus core system"));
        
        // Unset any existing primary for this group agent
        Optional<GroupAgentCoreBusSystem> existingPrimary = groupAgentCoreBusSystemRepository.findByGroupAgentAndIsPrimaryTrue(groupAgent);
        if (existingPrimary.isPresent() && !existingPrimary.get().getId().equals(groupAgentCoreBusSystem.getId())) {
            existingPrimary.get().setPrimary(false);
            groupAgentCoreBusSystemRepository.save(existingPrimary.get());
        }
        
        // Set this as primary
        groupAgentCoreBusSystem.setPrimary(true);
        return groupAgentCoreBusSystemRepository.save(groupAgentCoreBusSystem);
    }

    @Override
    public void removeGroupAgentFromBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem) {
        log.info("Removing group agent {} from bus core system {}", groupAgent.getName(), busCoreSystem.getName());
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemRepository.findByGroupAgentAndBusCoreSystem(groupAgent, busCoreSystem)
                .orElseThrow(() -> new RuntimeException("Group agent-Bus Core System relationship not found"));
        
        // Remove from group agent's collection
        groupAgent.removeCoreBusSystem(groupAgentCoreBusSystem);
        groupAgentRepository.save(groupAgent);
        
        // Delete the relationship
        groupAgentCoreBusSystemRepository.delete(groupAgentCoreBusSystem);
        log.info("Successfully removed group agent from bus core system");
    }

    @Override
    public void deleteGroupAgentCoreBusSystem(Long id) {
        log.info("Deleting group agent bus core system with ID: {}", id);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group agent bus core system not found with ID: " + id));
        
        // Remove from group agent's collection
        GroupAgent groupAgent = groupAgentCoreBusSystem.getGroupAgent();
        groupAgent.removeCoreBusSystem(groupAgentCoreBusSystem);
        groupAgentRepository.save(groupAgent);
        
        groupAgentCoreBusSystemRepository.delete(groupAgentCoreBusSystem);
        log.info("Successfully deleted group agent bus core system with ID: {}", id);
    }

    @Override
    public void deleteGroupAgentCoreBusSystemByUid(String uid) {
        log.info("Deleting group agent bus core system with UID: {}", uid);
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Group agent bus core system not found with UID: " + uid));
        
        // Remove from group agent's collection
        GroupAgent groupAgent = groupAgentCoreBusSystem.getGroupAgent();
        groupAgent.removeCoreBusSystem(groupAgentCoreBusSystem);
        groupAgentRepository.save(groupAgent);
        
        groupAgentCoreBusSystemRepository.delete(groupAgentCoreBusSystem);
        log.info("Successfully deleted group agent bus core system with UID: {}", uid);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isGroupAgentAssignedToBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem) {
        return groupAgentCoreBusSystemRepository.existsByGroupAgentAndBusCoreSystem(groupAgent, busCoreSystem);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByBusCoreSystemAndExternalAgentIdentifier(BusCoreSystem busCoreSystem, String externalAgentIdentifier) {
        return groupAgentCoreBusSystemRepository.existsByBusCoreSystemAndExternalAgentIdentifier(busCoreSystem, externalAgentIdentifier);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByGroupAgent(GroupAgent groupAgent) {
        return groupAgentCoreBusSystemRepository.countByGroupAgent(groupAgent);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByGroupAgent(GroupAgent groupAgent) {
        return groupAgentCoreBusSystemRepository.countByGroupAgentAndIsActiveTrue(groupAgent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgentCoreBusSystem> searchByGroupAgent(GroupAgent groupAgent, String searchTerm) {
        log.debug("Searching bus core systems for group agent {} with term: {}", groupAgent.getName(), searchTerm);
        return groupAgentCoreBusSystemRepository.findByGroupAgentAndSearchTerm(groupAgent, searchTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupAgentCoreBusSystem> searchByGroupAgent(GroupAgent groupAgent, String searchTerm, Pageable pageable) {
        log.debug("Searching bus core systems for group agent {} with term {} and pagination", groupAgent.getName(), searchTerm);
        return groupAgentCoreBusSystemRepository.findByGroupAgentAndSearchTerm(groupAgent, searchTerm, pageable);
    }

    @Override
    public void updateLastAuthentication(GroupAgentCoreBusSystem groupAgentCoreBusSystem) {
        groupAgentCoreBusSystem.updateLastAuthentication();
        groupAgentCoreBusSystemRepository.save(groupAgentCoreBusSystem);
    }

    @Override
    public void updateLastSync(GroupAgentCoreBusSystem groupAgentCoreBusSystem) {
        groupAgentCoreBusSystem.updateLastSync();
        groupAgentCoreBusSystemRepository.save(groupAgentCoreBusSystem);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean authenticateGroupAgentForBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem, String username, String password) {
        log.debug("Authenticating group agent {} for bus core system {}", groupAgent.getName(), busCoreSystem.getName());
        
        Optional<GroupAgentCoreBusSystem> groupAgentCoreBusSystemOpt = groupAgentCoreBusSystemRepository.findByGroupAgentAndBusCoreSystem(groupAgent, busCoreSystem);
        
        if (groupAgentCoreBusSystemOpt.isEmpty() || !groupAgentCoreBusSystemOpt.get().isActive()) {
            return false;
        }
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemOpt.get();
        
        // Check username and password
        if (!username.equals(groupAgentCoreBusSystem.getUsername()) || 
            !passwordEncoder.matches(password, groupAgentCoreBusSystem.getPassword())) {
            return false;
        }
        
        // Update last authentication
        updateLastAuthentication(groupAgentCoreBusSystem);
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DecryptedGroupAgentCredentials> getDecryptedCredentials(GroupAgent groupAgent, BusCoreSystem busCoreSystem) {
        log.debug("Getting decrypted credentials for group agent {} and bus core system {}", groupAgent.getName(), busCoreSystem.getName());
        
        Optional<GroupAgentCoreBusSystem> groupAgentCoreBusSystemOpt = groupAgentCoreBusSystemRepository.findByGroupAgentAndBusCoreSystem(groupAgent, busCoreSystem);
        
        if (groupAgentCoreBusSystemOpt.isEmpty()) {
            return Optional.empty();
        }
        
        GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemOpt.get();
        
        // Note: BCrypt passwords cannot be decrypted, so we return the encrypted password
        // In a real implementation, you might want to use a different encryption service for API keys
        String decryptedPassword = groupAgentCoreBusSystem.getPassword(); // BCrypt cannot be decrypted
        String decryptedApiKey = groupAgentCoreBusSystem.getApiKey(); // Assuming plain text for API keys
        String decryptedApiSecret = groupAgentCoreBusSystem.getApiSecret(); // Assuming plain text for API secrets
        String decryptedAccessToken = groupAgentCoreBusSystem.getAccessToken(); // Assuming plain text for tokens
        String decryptedRefreshToken = groupAgentCoreBusSystem.getRefreshToken(); // Assuming plain text for tokens
        
        DecryptedGroupAgentCredentials credentials = new DecryptedGroupAgentCredentials(
            groupAgentCoreBusSystem.getUsername(),
            decryptedPassword,
            decryptedApiKey,
            decryptedApiSecret,
            decryptedAccessToken,
            decryptedRefreshToken
        );
        
        return Optional.of(credentials);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgentCoreBusSystem> getWithRecentAuthentication(LocalDateTime since) {
        log.debug("Finding group agent bus core systems with recent authentication since: {}", since);
        return groupAgentCoreBusSystemRepository.findWithRecentAuthentication(since);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgentCoreBusSystem> getWithRecentSync(LocalDateTime since) {
        log.debug("Finding group agent bus core systems with recent sync since: {}", since);
        return groupAgentCoreBusSystemRepository.findWithRecentSync(since);
    }
}
