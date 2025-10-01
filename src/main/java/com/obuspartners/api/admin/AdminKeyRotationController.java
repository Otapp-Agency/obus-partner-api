package com.obuspartners.api.admin;

import com.obuspartners.modules.common.service.KeyRotationService;
import com.obuspartners.modules.common.util.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrative controller for encryption key rotation
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/v1/key-rotation")
@RequiredArgsConstructor
@Tag(name = "Admin Key Rotation", description = "Administrative endpoints for encryption key rotation")
public class AdminKeyRotationController {

    private final KeyRotationService keyRotationService;

    @GetMapping("/status")
    @Operation(summary = "Get key rotation status", description = "Retrieves the current key rotation configuration status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<KeyRotationService.KeyRotationStatus>> getKeyRotationStatus() {
        log.info("Getting key rotation status");
        
        KeyRotationService.KeyRotationStatus status = keyRotationService.getKeyRotationStatus();
        
        return ResponseEntity.ok(new ResponseWrapper<>(
                true,
                200,
                "Key rotation status retrieved successfully",
                status
        ));
    }

    @PostMapping("/validate-keys")
    @Operation(summary = "Validate encryption keys", description = "Validates that encryption keys are properly configured and working")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<String>> validateEncryptionKeys() {
        log.info("Validating encryption keys");
        
        try {
            keyRotationService.validateEncryptionKeys();
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    200,
                    "Encryption keys validation successful",
                    "All encryption keys are properly configured"
            ));
                    
        } catch (Exception e) {
            log.error("Encryption keys validation failed", e);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    false,
                    500,
                    "Encryption keys validation failed: " + e.getMessage(),
                    null
            ));
        }
    }

    @PostMapping("/rotate-agent-passwords")
    @Operation(summary = "Rotate agent passwords", description = "Rotates encryption keys for all AgentBusCoreSystem passwords")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<String>> rotateAgentPasswords() {
        log.info("Starting key rotation for agent passwords");
        
        try {
            keyRotationService.rotateAllAgentBusCoreSystemPasswords();
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    200,
                    "Key rotation completed successfully",
                    "All agent passwords have been rotated to use the new encryption key"
            ));
                    
        } catch (Exception e) {
            log.error("Key rotation failed", e);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    false,
                    500,
                    "Key rotation failed: " + e.getMessage(),
                    null
            ));
        }
    }
}
