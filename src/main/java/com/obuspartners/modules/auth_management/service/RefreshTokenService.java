package com.obuspartners.modules.auth_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.obuspartners.modules.auth_management.domain.entity.RefreshToken;
import com.obuspartners.modules.auth_management.repository.RefreshTokenRepository;
import com.obuspartners.modules.auth_management.util.JwtUtil;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing refresh tokens
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Service
@Transactional
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * Create a new refresh token for a user
     */
    public RefreshToken createRefreshToken(User user) {
        // Revoke existing tokens for the user
        refreshTokenRepository.revokeAllTokensByUser(user);

        // Generate new refresh token
        String tokenString = jwtUtil.generateRefreshTokenString();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshExpiration);

        RefreshToken refreshToken = new RefreshToken(tokenString, user, expiryDate);
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Validate and get refresh token
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verify if refresh token is valid and active
     */
    public boolean verifyRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = findByToken(token);
        if (refreshTokenOpt.isEmpty()) {
            return false;
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        return refreshToken.isActive();
    }

    /**
     * Revoke refresh token
     */
    public void revokeRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = findByToken(token);
        if (refreshTokenOpt.isPresent()) {
            RefreshToken refreshToken = refreshTokenOpt.get();
            refreshToken.setIsRevoked(true);
            refreshTokenRepository.save(refreshToken);
        }
    }

    /**
     * Revoke all refresh tokens for a user
     */
    public void revokeAllTokensByUser(User user) {
        refreshTokenRepository.revokeAllTokensByUser(user);
    }

    /**
     * Clean up expired tokens
     */
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * Clean up revoked tokens
     */
    public void cleanupRevokedTokens() {
        refreshTokenRepository.deleteRevokedTokens();
    }

    /**
     * Get active token count for a user
     */
    public long getActiveTokenCount(User user) {
        return refreshTokenRepository.countActiveTokensByUser(user, LocalDateTime.now());
    }
}
