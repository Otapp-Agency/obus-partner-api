package com.obuspartners.modules.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Implementation of PasswordEncryptionService using AES encryption
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class PasswordEncryptionServiceImpl implements PasswordEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String ENCRYPTION_PREFIX = "ENC:";
    
    @Value("${app.encryption.primary-key:}")
    private String primaryEncryptionKey;
    
    @Value("${app.encryption.secondary-key:}")
    private String secondaryEncryptionKey;
    
    @Value("${app.encryption.key-rotation.enabled:false}")
    private boolean keyRotationEnabled;

    @Override
    public String encryptPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            return plainTextPassword;
        }
        
        try {
            // Check if already encrypted
            if (isEncrypted(plainTextPassword)) {
                log.warn("Attempting to encrypt an already encrypted password");
                return plainTextPassword;
            }
            
            // Validate primary key is configured
            if (primaryEncryptionKey == null || primaryEncryptionKey.trim().isEmpty()) {
                throw new IllegalStateException("Primary encryption key is not configured. Please set app.encryption.primary-key in application.yml");
            }
            
            SecretKeySpec secretKey = new SecretKeySpec(normalizeKey(primaryEncryptionKey), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(plainTextPassword.getBytes(StandardCharsets.UTF_8));
            String encryptedPassword = Base64.getEncoder().encodeToString(encryptedBytes);
            
            log.debug("Password encrypted successfully with primary key");
            return ENCRYPTION_PREFIX + encryptedPassword;
            
        } catch (Exception e) {
            log.error("Failed to encrypt password", e);
            throw new RuntimeException("Password encryption failed", e);
        }
    }

    @Override
    public String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.trim().isEmpty()) {
            return encryptedPassword;
        }
        
        try {
            // Check if password is encrypted
            if (!isEncrypted(encryptedPassword)) {
                log.debug("Password is not encrypted, returning as-is");
                return encryptedPassword;
            }
            
            // Remove encryption prefix
            String base64EncryptedPassword = encryptedPassword.substring(ENCRYPTION_PREFIX.length());
            
            // Try primary key first
            try {
                return decryptWithKey(base64EncryptedPassword, primaryEncryptionKey, "primary");
            } catch (Exception e) {
                // If key rotation is enabled and primary key fails, try secondary key
                if (keyRotationEnabled && secondaryEncryptionKey != null && !secondaryEncryptionKey.trim().isEmpty()) {
                    log.debug("Primary key decryption failed, trying secondary key");
                    try {
                        return decryptWithKey(base64EncryptedPassword, secondaryEncryptionKey, "secondary");
                    } catch (Exception e2) {
                        log.error("Both primary and secondary key decryption failed", e2);
                        throw new RuntimeException("Password decryption failed with both keys", e2);
                    }
                } else {
                    log.error("Primary key decryption failed and key rotation is disabled or secondary key not configured", e);
                    throw new RuntimeException("Password decryption failed", e);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to decrypt password", e);
            throw new RuntimeException("Password decryption failed", e);
        }
    }
    
    /**
     * Decrypt password with a specific key
     */
    private String decryptWithKey(String base64EncryptedPassword, String key, String keyType) throws Exception {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException(keyType + " encryption key is not configured");
        }
        
        SecretKeySpec secretKey = new SecretKeySpec(normalizeKey(key), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        byte[] encryptedBytes = Base64.getDecoder().decode(base64EncryptedPassword);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        
        log.debug("Password decrypted successfully with " + keyType + " key");
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    @Override
    public boolean isEncrypted(String password) {
        return password != null && password.startsWith(ENCRYPTION_PREFIX);
    }

    @Override
    public String rotateEncryptionKey(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.trim().isEmpty()) {
            return encryptedPassword;
        }
        
        if (!isEncrypted(encryptedPassword)) {
            log.warn("Attempting to rotate key for non-encrypted password");
            return encryptedPassword;
        }
        
        try {
            // Decrypt with current key (could be primary or secondary)
            String plainTextPassword = decryptPassword(encryptedPassword);
            
            // Re-encrypt with primary key
            String reEncryptedPassword = encryptPassword(plainTextPassword);
            
            log.info("Successfully rotated encryption key");
            return reEncryptedPassword;
            
        } catch (Exception e) {
            log.error("Failed to rotate encryption key", e);
            throw new RuntimeException("Key rotation failed", e);
        }
    }

    /**
     * Normalize the encryption key to ensure it's exactly 32 bytes for AES-256
     * 
     * @param key the raw encryption key
     * @return normalized 32-byte key
     */
    private byte[] normalizeKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Encryption key cannot be null or empty");
        }
        
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        
        // If key is exactly 32 bytes, return as is
        if (keyBytes.length == 32) {
            return keyBytes;
        }
        
        // If key is shorter than 32 bytes, pad with zeros
        if (keyBytes.length < 32) {
            byte[] normalizedKey = new byte[32];
            System.arraycopy(keyBytes, 0, normalizedKey, 0, keyBytes.length);
            return normalizedKey;
        }
        
        // If key is longer than 32 bytes, truncate to 32 bytes
        if (keyBytes.length > 32) {
            byte[] normalizedKey = new byte[32];
            System.arraycopy(keyBytes, 0, normalizedKey, 0, 32);
            return normalizedKey;
        }
        
        return keyBytes;
    }

    /**
     * Generate a secure encryption key (utility method)
     * This should be used to generate the initial key and then stored securely
     * 
     * @return a base64 encoded encryption key
     */
    public static String generateEncryptionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // 256-bit key
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
}
