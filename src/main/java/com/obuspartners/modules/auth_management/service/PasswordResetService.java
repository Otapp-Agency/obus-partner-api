package com.obuspartners.modules.auth_management.service;

import com.obuspartners.modules.auth_management.domain.entity.PasswordResetToken;
import com.obuspartners.modules.auth_management.repository.PasswordResetTokenRepository;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.common.service.EmailService;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for handling password reset functionality
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${app.password-reset.token-expiration-hours:24}")
    private int tokenExpirationHours;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send password reset email to user
     * 
     * @param email the user's email
     */
    public void sendPasswordResetEmail(String email) {
        log.debug("Sending password reset email to: {}", email);

        // Find user by email
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.debug("User not found with email: {}", email);
            return; // Don't reveal if user exists or not
        }

        User user = userOpt.get();

        // Mark any existing tokens as used
        passwordResetTokenRepository.markAllTokensAsUsedForUser(user);

        // Generate new reset token
        String token = generateResetToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpirationHours);

        // Save reset token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(expiresAt);
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        // Send email
        sendPasswordResetEmail(user, token);

        log.info("Password reset email sent to user: {}", user.getUsername());
    }

    /**
     * Reset password using token
     * 
     * @param token the reset token
     * @param newPassword the new password
     */
    public void resetPassword(String token, String newPassword) {
        log.debug("Resetting password with token: {}", token);

        // Find token
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            throw new ApiException("Invalid or expired reset token", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        PasswordResetToken resetToken = tokenOpt.get();

        // Check if token is expired
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("Reset token has expired", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Check if token is already used
        if (resetToken.getUsed()) {
            throw new ApiException("Reset token has already been used", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Reset password
        User user = resetToken.getUser();
        userService.changePassword(user.getId(), newPassword);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getUsername());
    }

    /**
     * Generate a secure random reset token
     * 
     * @return the generated token
     */
    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Send password reset email to user
     * 
     * @param user the user
     * @param token the reset token
     */
    private void sendPasswordResetEmail(User user, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        
        String subject = "Password Reset Request - OBUS Partners";
        String body = String.format(
                "Hello %s,\n\n" +
                "You have requested to reset your password for your OBUS Partners account.\n\n" +
                "To reset your password, please click the link below:\n" +
                "%s\n\n" +
                "This link will expire in %d hours for security reasons.\n\n" +
                "If you did not request this password reset, please ignore this email and your password will remain unchanged.\n\n" +
                "Best regards,\n" +
                "OBUS Partners Team",
                user.getDisplayName(),
                resetUrl,
                tokenExpirationHours
        );

        try {
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            log.error("Failed to send password reset email to user: {}", user.getUsername(), e);
            throw new ApiException("Failed to send password reset email", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
