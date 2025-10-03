package com.obuspartners.modules.agent_management.service;

import com.obuspartners.modules.agent_management.domain.entity.GroupAgent;
import com.obuspartners.modules.agent_management.domain.entity.GroupAgentCoreBusSystem;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for GroupAgentCoreBusSystem management operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface GroupAgentCoreBusSystemService {

    /**
     * Assign GroupAgent to BusCoreSystem with credentials
     * 
     * @param groupAgent the group agent
     * @param busCoreSystem the bus core system
     * @param externalAgentIdentifier the external agent identifier
     * @param username the username
     * @param password the password (will be encrypted)
     * @param apiKey the API key (optional)
     * @param apiSecret the API secret (optional)
     * @param isPrimary whether this is the primary bus core system
     * @return the created GroupAgentCoreBusSystem
     */
    GroupAgentCoreBusSystem assignGroupAgentToBusCoreSystem(
            GroupAgent groupAgent, 
            BusCoreSystem busCoreSystem,
            String externalAgentIdentifier,
            String username,
            String password,
            String apiKey,
            String apiSecret,
            boolean isPrimary
    );

    /**
     * Update GroupAgent bus core system credentials
     * 
     * @param groupAgentCoreBusSystem the group agent bus core system to update
     * @return the updated GroupAgentCoreBusSystem
     */
    GroupAgentCoreBusSystem updateGroupAgentBusCoreSystemCredentials(GroupAgentCoreBusSystem groupAgentCoreBusSystem);

    /**
     * Update GroupAgent bus core system credentials by ID
     * 
     * @param id the group agent core bus system ID
     * @param username the username
     * @param password the password (will be encrypted)
     * @param apiKey the API key (optional)
     * @param apiSecret the API secret (optional)
     * @return the updated GroupAgentCoreBusSystem
     */
    GroupAgentCoreBusSystem updateCredentialsById(Long id, String username, String password, String apiKey, String apiSecret);

    /**
     * Update GroupAgent bus core system credentials by UID
     * 
     * @param uid the group agent core bus system UID
     * @param username the username
     * @param password the password (will be encrypted)
     * @param apiKey the API key (optional)
     * @param apiSecret the API secret (optional)
     * @return the updated GroupAgentCoreBusSystem
     */
    GroupAgentCoreBusSystem updateCredentialsByUid(String uid, String username, String password, String apiKey, String apiSecret);

    /**
     * Get GroupAgentCoreBusSystem by ID
     * 
     * @param id the group agent core bus system ID
     * @return the group agent core bus system if found
     */
    Optional<GroupAgentCoreBusSystem> getGroupAgentCoreBusSystemById(Long id);

    /**
     * Get GroupAgentCoreBusSystem by UID
     * 
     * @param uid the group agent core bus system UID
     * @return the group agent core bus system if found
     */
    Optional<GroupAgentCoreBusSystem> getGroupAgentCoreBusSystemByUid(String uid);

    /**
     * Get GroupAgentCoreBusSystem by GroupAgent and BusCoreSystem
     * 
     * @param groupAgent the group agent
     * @param busCoreSystem the bus core system
     * @return the group agent core bus system if found
     */
    Optional<GroupAgentCoreBusSystem> getByGroupAgentAndBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem);

    /**
     * Get all bus core systems for a GroupAgent
     * 
     * @param groupAgent the group agent
     * @return list of group agent core bus systems
     */
    List<GroupAgentCoreBusSystem> getBusCoreSystemsByGroupAgent(GroupAgent groupAgent);

    /**
     * Get all bus core systems for a GroupAgent with pagination
     * 
     * @param groupAgent the group agent
     * @param pageable pagination parameters
     * @return page of group agent core bus systems
     */
    Page<GroupAgentCoreBusSystem> getBusCoreSystemsByGroupAgent(GroupAgent groupAgent, Pageable pageable);

    /**
     * Get active bus core systems for a GroupAgent
     * 
     * @param groupAgent the group agent
     * @return list of active group agent core bus systems
     */
    List<GroupAgentCoreBusSystem> getActiveBusCoreSystemsByGroupAgent(GroupAgent groupAgent);

    /**
     * Get primary bus core system for a GroupAgent
     * 
     * @param groupAgent the group agent
     * @return the primary group agent core bus system if found
     */
    Optional<GroupAgentCoreBusSystem> getPrimaryBusCoreSystemByGroupAgent(GroupAgent groupAgent);

    /**
     * Get bus core systems by BusCoreSystem
     * 
     * @param busCoreSystem the bus core system
     * @return list of group agent core bus systems
     */
    List<GroupAgentCoreBusSystem> getByBusCoreSystem(BusCoreSystem busCoreSystem);

    /**
     * Get active bus core systems by BusCoreSystem
     * 
     * @param busCoreSystem the bus core system
     * @return list of active group agent core bus systems
     */
    List<GroupAgentCoreBusSystem> getActiveByBusCoreSystem(BusCoreSystem busCoreSystem);

    /**
     * Activate GroupAgent for BusCoreSystem
     * 
     * @param groupAgentCoreBusSystem the group agent core bus system
     * @return the activated group agent core bus system
     */
    GroupAgentCoreBusSystem activateGroupAgentForBusCoreSystem(GroupAgentCoreBusSystem groupAgentCoreBusSystem);

    /**
     * Deactivate GroupAgent for BusCoreSystem
     * 
     * @param groupAgentCoreBusSystem the group agent core bus system
     * @return the deactivated group agent core bus system
     */
    GroupAgentCoreBusSystem deactivateGroupAgentForBusCoreSystem(GroupAgentCoreBusSystem groupAgentCoreBusSystem);

    /**
     * Set primary BusCoreSystem for GroupAgent
     * 
     * @param groupAgent the group agent
     * @param busCoreSystem the bus core system to set as primary
     * @return the updated GroupAgentCoreBusSystem
     */
    GroupAgentCoreBusSystem setPrimaryBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem);

    /**
     * Remove GroupAgent from BusCoreSystem
     * 
     * @param groupAgent the group agent
     * @param busCoreSystem the bus core system
     */
    void removeGroupAgentFromBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem);

    /**
     * Delete GroupAgentCoreBusSystem by ID
     * 
     * @param id the group agent core bus system ID
     */
    void deleteGroupAgentCoreBusSystem(Long id);

    /**
     * Delete GroupAgentCoreBusSystem by UID
     * 
     * @param uid the group agent core bus system UID
     */
    void deleteGroupAgentCoreBusSystemByUid(String uid);

    /**
     * Check if GroupAgent is assigned to BusCoreSystem
     * 
     * @param groupAgent the group agent
     * @param busCoreSystem the bus core system
     * @return true if assigned, false otherwise
     */
    boolean isGroupAgentAssignedToBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem);

    /**
     * Check if external agent identifier exists for BusCoreSystem
     * 
     * @param busCoreSystem the bus core system
     * @param externalAgentIdentifier the external agent identifier
     * @return true if exists, false otherwise
     */
    boolean existsByBusCoreSystemAndExternalAgentIdentifier(BusCoreSystem busCoreSystem, String externalAgentIdentifier);

    /**
     * Count bus core systems by GroupAgent
     * 
     * @param groupAgent the group agent
     * @return count of bus core systems
     */
    long countByGroupAgent(GroupAgent groupAgent);

    /**
     * Count active bus core systems by GroupAgent
     * 
     * @param groupAgent the group agent
     * @return count of active bus core systems
     */
    long countActiveByGroupAgent(GroupAgent groupAgent);

    /**
     * Search bus core systems by GroupAgent and search term
     * 
     * @param groupAgent the group agent
     * @param searchTerm the search term
     * @return list of matching group agent core bus systems
     */
    List<GroupAgentCoreBusSystem> searchByGroupAgent(GroupAgent groupAgent, String searchTerm);

    /**
     * Search bus core systems by GroupAgent and search term with pagination
     * 
     * @param groupAgent the group agent
     * @param searchTerm the search term
     * @param pageable pagination parameters
     * @return page of matching group agent core bus systems
     */
    Page<GroupAgentCoreBusSystem> searchByGroupAgent(GroupAgent groupAgent, String searchTerm, Pageable pageable);

    /**
     * Update last authentication date
     * 
     * @param groupAgentCoreBusSystem the group agent core bus system
     */
    void updateLastAuthentication(GroupAgentCoreBusSystem groupAgentCoreBusSystem);

    /**
     * Update last sync date
     * 
     * @param groupAgentCoreBusSystem the group agent core bus system
     */
    void updateLastSync(GroupAgentCoreBusSystem groupAgentCoreBusSystem);

    /**
     * Authenticate GroupAgent for BusCoreSystem
     * 
     * @param groupAgent the group agent
     * @param busCoreSystem the bus core system
     * @param username the username
     * @param password the password
     * @return true if authentication successful, false otherwise
     */
    boolean authenticateGroupAgentForBusCoreSystem(GroupAgent groupAgent, BusCoreSystem busCoreSystem, String username, String password);

    /**
     * Get decrypted credentials for GroupAgent and BusCoreSystem
     * 
     * @param groupAgent the group agent
     * @param busCoreSystem the bus core system
     * @return the decrypted credentials if found
     */
    Optional<DecryptedGroupAgentCredentials> getDecryptedCredentials(GroupAgent groupAgent, BusCoreSystem busCoreSystem);

    /**
     * Get GroupAgents with recent authentication
     * 
     * @param since the date since when to look for authentication
     * @return list of group agent core bus systems with recent authentication
     */
    List<GroupAgentCoreBusSystem> getWithRecentAuthentication(java.time.LocalDateTime since);

    /**
     * Get GroupAgents with recent sync
     * 
     * @param since the date since when to look for sync
     * @return list of group agent core bus systems with recent sync
     */
    List<GroupAgentCoreBusSystem> getWithRecentSync(java.time.LocalDateTime since);

    /**
     * Data class for decrypted credentials
     */
    class DecryptedGroupAgentCredentials {
        private String username;
        private String password;
        private String apiKey;
        private String apiSecret;
        private String accessToken;
        private String refreshToken;

        public DecryptedGroupAgentCredentials(String username, String password, String apiKey, String apiSecret, String accessToken, String refreshToken) {
            this.username = username;
            this.password = password;
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        // Getters
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getApiKey() { return apiKey; }
        public String getApiSecret() { return apiSecret; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}
