package com.obuspartners.api.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.obuspartners.modules.auth_management.domain.dto.ChangePasswordRequest;
import com.obuspartners.modules.auth_management.domain.dto.ConfirmPasswordResetRequest;
import com.obuspartners.modules.auth_management.domain.dto.ResetPasswordRequest;
import com.obuspartners.modules.auth_management.service.PasswordResetService;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.common.exception.ResourceNotFoundException;
import com.obuspartners.modules.common.util.ResponseWrapper;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.service.UserService;

import java.util.Objects;

/**
 * Password management controller for changing and resetting passwords
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/v1/auth/password")
@CrossOrigin(origins = "*")
@Tag(name = "Auth", description = "Authentication APIs - Login, Register, Refresh Token, Password Reset")
public class PasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Change password endpoint (user must be authenticated)
     * 
     * @param request the password change request
     * @param authentication the current authentication
     * @return success message
     */
    @PostMapping("/change")
    public ResponseEntity<ResponseWrapper<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        
        // Validate password confirmation
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new ApiException("New password and confirm password do not match", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Get current user
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            currentUser.getUsername(),
                            request.getCurrentPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ApiException("Current password is incorrect", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Change password
        userService.changePassword(currentUser.getId(), request.getNewPassword());

        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Password changed successfully", null));
    }

    /**
     * Request password reset via email
     * 
     * @param request the password reset request
     * @return success message
     */
    @PostMapping("/reset")
    public ResponseEntity<ResponseWrapper<String>> requestPasswordReset(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        // Check if user exists with this email
        if (!userService.existsByEmail(request.getEmail())) {
            // Return success even if email doesn't exist for security reasons
            return ResponseEntity.ok(new ResponseWrapper<>(true, 200, 
                    "If an account with that email exists, a password reset link has been sent", null));
        }

        // Send password reset email
        passwordResetService.sendPasswordResetEmail(request.getEmail());

        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, 
                "If an account with that email exists, a password reset link has been sent", null));
    }

    /**
     * Confirm password reset with token
     * 
     * @param request the password reset confirmation request
     * @return success message
     */
    @PostMapping("/confirm-reset")
    public ResponseEntity<ResponseWrapper<String>> confirmPasswordReset(
            @Valid @RequestBody ConfirmPasswordResetRequest request) {
        
        // Validate password confirmation
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new ApiException("New password and confirm password do not match", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Reset password
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Password reset successfully", null));
    }
}
