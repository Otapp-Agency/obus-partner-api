package com.obuspartners.modules.common.service;

import com.obuspartners.modules.agent_management.domain.entity.AgentBusCoreSystem;
import com.obuspartners.modules.agent_management.service.AgentBusCoreSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing encryption key rotation
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeyRotationService {

    private final PasswordEncryptionService passwordEncryptionService;
    private final AgentBusCoreSystemService agentBusCoreSystemService;

    @Value("${app.encryption.key-rotation.batch-size:100}")
    private int batchSize;

    @Value("${app.encryption.key-rotation.enabled:false}")
    private boolean keyRotationEnabled;

    /**
     * Rotate encryption keys for all AgentBusCoreSystem passwords
     * This should be run when changing encryption keys
     */
    @Transactional
    public void rotateAllAgentBusCoreSystemPasswords() {
        if (!keyRotationEnabled) {
            throw new IllegalStateException("Key rotation is not enabled. Set app.encryption.key-rotation.enabled=true");
        }

        log.info("Starting encryption key rotation for all AgentBusCoreSystem passwords");
        
        try {
            // Get all agent bus core systems that need key rotation
            List<AgentBusCoreSystem> agentBusCoreSystems = 
                agentBusCoreSystemService.findAllForKeyRotation();
            
            int totalCount = agentBusCoreSystems.size();
            int processedCount = 0;
            int rotatedCount = 0;
            
            log.info("Found {} AgentBusCoreSystem records to process", totalCount);
            
            for (AgentBusCoreSystem agentBusCoreSystem : agentBusCoreSystems) {
                try {
                    boolean needsRotation = false;
                    String newPassword = null;
                    String newTxnPassword = null;
                    
                    // Check if password needs rotation
                    if (agentBusCoreSystem.getEncryptedPassword() != null) {
                        newPassword = passwordEncryptionService.rotateEncryptionKey(agentBusCoreSystem.getEncryptedPassword());
                        needsRotation = true;
                    }
                    
                    // Check if transaction password needs rotation
                    if (agentBusCoreSystem.getEncryptedTxnPassword() != null) {
                        newTxnPassword = passwordEncryptionService.rotateEncryptionKey(agentBusCoreSystem.getEncryptedTxnPassword());
                        needsRotation = true;
                    }
                    
                    // Update if rotation is needed
                    if (needsRotation) {
                        if (newPassword != null) {
                            agentBusCoreSystem.setEncryptedPassword(newPassword);
                        }
                        if (newTxnPassword != null) {
                            agentBusCoreSystem.setEncryptedTxnPassword(newTxnPassword);
                        }
                        
                        agentBusCoreSystemService.save(agentBusCoreSystem);
                        rotatedCount++;
                    }
                    
                    processedCount++;
                    
                    // Log progress every batch
                    if (processedCount % batchSize == 0) {
                        log.info("Processed {}/{} records, rotated {} passwords", processedCount, totalCount, rotatedCount);
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to rotate keys for AgentBusCoreSystem ID: {}", agentBusCoreSystem.getId(), e);
                    // Continue with next record
                }
            }
            
            log.info("Key rotation completed. Processed: {}, Rotated: {}", processedCount, rotatedCount);
            
        } catch (Exception e) {
            log.error("Key rotation failed", e);
            throw new RuntimeException("Key rotation failed", e);
        }
    }

    /**
     * Validate encryption keys are properly configured
     */
    public void validateEncryptionKeys() {
        log.info("Validating encryption key configuration");
        
        try {
            // Test encryption and decryption
            String testPassword = "test-password-123";
            String encrypted = passwordEncryptionService.encryptPassword(testPassword);
            String decrypted = passwordEncryptionService.decryptPassword(encrypted);
            
            if (!testPassword.equals(decrypted)) {
                throw new RuntimeException("Encryption/decryption test failed");
            }
            
            log.info("Encryption keys validation successful");
            
        } catch (Exception e) {
            log.error("Encryption keys validation failed", e);
            throw new RuntimeException("Encryption keys validation failed", e);
        }
    }

    /**
     * Get key rotation status
     */
    public KeyRotationStatus getKeyRotationStatus() {
        return KeyRotationStatus.builder()
                .enabled(keyRotationEnabled)
                .batchSize(batchSize)
                .build();
    }

    /**
     * Key rotation status DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KeyRotationStatus {
        private boolean enabled;
        private int batchSize;
    }
}
