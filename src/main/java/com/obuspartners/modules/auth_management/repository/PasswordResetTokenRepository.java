package com.obuspartners.modules.auth_management.repository;

import com.obuspartners.modules.auth_management.domain.entity.PasswordResetToken;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for PasswordResetToken entities
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find password reset token by token string
     * 
     * @param token the token string
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find active (non-expired and unused) password reset token by user
     * 
     * @param user the user
     * @param now current time
     * @return Optional containing the active token if found
     */
    Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiresAtAfter(User user, LocalDateTime now);

    /**
     * Mark all tokens for a user as used
     * 
     * @param user the user
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.user = :user AND prt.used = false")
    void markAllTokensAsUsedForUser(@Param("user") User user);

    /**
     * Delete expired tokens
     * 
     * @param now current time
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
