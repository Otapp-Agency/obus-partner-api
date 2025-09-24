package com.obuspartners.modules.auth_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for cleaning up expired and revoked tokens
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Service
public class TokenCleanupService {

    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * Clean up expired tokens every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        try {
            refreshTokenService.cleanupExpiredTokens();
            System.out.println("Expired tokens cleaned up successfully");
        } catch (Exception e) {
            System.err.println("Error cleaning up expired tokens: " + e.getMessage());
        }
    }

    /**
     * Clean up revoked tokens every 6 hours
     */
    @Scheduled(fixedRate = 21600000) // 6 hours in milliseconds
    public void cleanupRevokedTokens() {
        try {
            refreshTokenService.cleanupRevokedTokens();
            System.out.println("Revoked tokens cleaned up successfully");
        } catch (Exception e) {
            System.err.println("Error cleaning up revoked tokens: " + e.getMessage());
        }
    }
}
