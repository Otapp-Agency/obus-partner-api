package com.obuspartners.modules.common.service;

/**
 * Service interface for password encryption and decryption
 * Used for sensitive credentials that need to be stored encrypted
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface PasswordEncryptionService {

    /**
     * Encrypt a plain text password
     * 
     * @param plainTextPassword the plain text password to encrypt
     * @return the encrypted password
     */
    String encryptPassword(String plainTextPassword);

    /**
     * Decrypt an encrypted password
     * 
     * @param encryptedPassword the encrypted password to decrypt
     * @return the plain text password
     */
    String decryptPassword(String encryptedPassword);

    /**
     * Check if a password is encrypted (contains encryption markers)
     * 
     * @param password the password to check
     * @return true if the password appears to be encrypted
     */
    boolean isEncrypted(String password);

    /**
     * Rotate encryption keys - re-encrypt with new primary key
     * This method should be used when changing encryption keys
     * 
     * @param encryptedPassword the currently encrypted password
     * @return password encrypted with the new primary key
     */
    String rotateEncryptionKey(String encryptedPassword);
}
