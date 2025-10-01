package com.obuspartners.api.admin;

import com.obuspartners.modules.auth_management.domain.dto.ChangePasswordRequest;
import com.obuspartners.modules.common.util.ResponseWrapper;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for user management operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/v1/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Administrative endpoints for managing users")
public class AdminUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/change-password")
    @Operation(summary = "Change user password", description = "Changes the password for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ResponseWrapper<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Changing password for user: {}", username);
        
        try {
            // Get current user
            User currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                log.warn("Invalid current password for user: {}", username);
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, 400, "Current password is incorrect", null));
            }
            
            // Validate new password confirmation
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                log.warn("Password confirmation mismatch for user: {}", username);
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, 400, "New password and confirmation do not match", null));
            }
            
            // Change password
            userService.changePassword(currentUser.getId(), request.getNewPassword());
            
            log.info("Password changed successfully for user: {}", username);
            return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Password changed successfully", null));
            
        } catch (Exception e) {
            log.error("Error changing password for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<>(false, 500, "Error changing password: " + e.getMessage(), null));
        }
    }
}
