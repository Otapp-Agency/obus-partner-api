package com.obuspartners.api.auth;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.obuspartners.modules.auth_management.domain.dto.AuthResponse;
import com.obuspartners.modules.auth_management.domain.dto.LoginRequest;
import com.obuspartners.modules.auth_management.domain.dto.RefreshTokenRequest;
import com.obuspartners.modules.auth_management.domain.dto.RegisterRequestDto;
import com.obuspartners.modules.auth_management.service.RefreshTokenService;
import com.obuspartners.modules.auth_management.util.JwtUtil;
import com.obuspartners.modules.common.exception.DuplicateResourceException;
import com.obuspartners.modules.common.util.ResponseWrapper;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserType;
import com.obuspartners.modules.user_and_role_management.domain.entity.User;
import com.obuspartners.modules.user_and_role_management.service.UserService;

/**
 * Authentication controller for login and registration
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * Login endpoint
     * 
     * @param loginRequest the login request
     * @return authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Get user details
        User user = userService.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate JWT token - limited scope if password change required
        String jwt;
        if (user.getRequirePasswordChange()) {
            jwt = jwtUtil.generateLimitedToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal(), "password_change");
        } else {
            jwt = jwtUtil.generateToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());
        }
        
        // Create refresh token only if password change is not required
        com.obuspartners.modules.auth_management.domain.entity.RefreshToken refreshToken = null;
        if (!user.getRequirePasswordChange()) {
            refreshToken = refreshTokenService.createRefreshToken(user);
        }
        
        // Create response
        AuthResponse response = AuthResponse.builder()
            .accessToken(jwt)
            .refreshToken(refreshToken != null ? refreshToken.getToken() : null)
            .username(user.getUsername())
            .email(user.getEmail())
            .userType(user.getUserType().name())
            .requireResetPassword(user.getRequirePasswordChange())
            .partnerId(user.getPartner() != null ? user.getPartner().getId() : null)
            .partnerUid(user.getPartner() != null ? user.getPartner().getUid() : null)
            .partnerCode(user.getPartner() != null ? user.getPartner().getCode() : null)
            .partnerBusinessName(user.getPartner() != null ? user.getPartner().getBusinessName() : null)
            // Essential frontend fields
            .displayName(user.getDisplayName())
            .roles(user.getRoles().stream().map(role -> role.getRoleType().getValue()).toList())
            .tokenExpiresAt(java.time.format.DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").format(jwtUtil.getExpirationDateFromToken(jwt).toInstant().atZone(java.time.ZoneOffset.UTC).toLocalDateTime()))
            .agentId(user.getAgent() != null ? user.getAgent().getId() : null)
            .agentStatus(user.getAgent() != null ? user.getAgent().getStatus().name() : null)
            .lastLoginAt(user.getUpdatedAt() != null ? java.time.format.DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").format(user.getUpdatedAt()) : null)
            .build();

        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Login successful", response));
    }

    /**
     * Register endpoint
     * 
     * @param registerRequest the registration request
     * @return success message
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<String>> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        // Check if username already exists
        if (userService.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateResourceException("Username is already taken!");
        }

        // Check if email already exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email is already in use!");
        }

        // Create new user
        User user = new User(
            registerRequest.getUsername(),
            registerRequest.getEmail(),
            registerRequest.getPassword(),
            registerRequest.getDisplayName(),
            UserType.AGENT
        );
        
        // Save user (password will be encoded in UserService)
        userService.save(user);

        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "User registered successfully", null));
    }

    /**
     * Refresh token endpoint
     * 
     * @param refreshTokenRequest the refresh token request
     * @return new access token and refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ResponseWrapper<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        // Verify refresh token
        if (!refreshTokenService.verifyRefreshToken(refreshTokenRequest.getRefreshToken())) {
            return ResponseEntity.status(401)
                .body(new ResponseWrapper<>(false, 401, "Invalid or expired refresh token", null));
        }

        // Get refresh token entity
        com.obuspartners.modules.auth_management.domain.entity.RefreshToken refreshToken = 
            refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Get user details
        User user = refreshToken.getUser();
        
        // Generate new access token - maintain limited scope if password change required
        String newAccessToken;
        if (user.getRequirePasswordChange()) {
            newAccessToken = jwtUtil.generateLimitedToken(user, "password_change");
        } else {
            newAccessToken = jwtUtil.generateToken(user);
        }
        
        // Create new refresh token (rotate refresh token)
        com.obuspartners.modules.auth_management.domain.entity.RefreshToken newRefreshToken = 
            refreshTokenService.createRefreshToken(user);
        
        // Revoke old refresh token
        refreshTokenService.revokeRefreshToken(refreshTokenRequest.getRefreshToken());
        
        // Create response
        AuthResponse response = AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken.getToken())
            .username(user.getUsername())
            .email(user.getEmail())
            .userType(user.getUserType().name())
            .requireResetPassword(user.getRequirePasswordChange())
            .partnerId(user.getPartner() != null ? user.getPartner().getId() : null)
            .partnerUid(user.getPartner() != null ? user.getPartner().getUid() : null)
            .partnerCode(user.getPartner() != null ? user.getPartner().getCode() : null)
            .partnerBusinessName(user.getPartner() != null ? user.getPartner().getBusinessName() : null)
            // Essential frontend fields
            .displayName(user.getDisplayName())
            .roles(user.getRoles().stream().map(role -> role.getRoleType().getValue()).toList())
            .tokenExpiresAt(java.time.format.DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").format(jwtUtil.getExpirationDateFromToken(newAccessToken).toInstant().atZone(java.time.ZoneOffset.UTC).toLocalDateTime()))
            .agentId(user.getAgent() != null ? user.getAgent().getId() : null)
            .agentStatus(user.getAgent() != null ? user.getAgent().getStatus().name() : null)
            .lastLoginAt(user.getUpdatedAt() != null ? java.time.format.DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").format(user.getUpdatedAt()) : null)
            .build();

        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Token refreshed successfully", response));
    }

    /**
     * Logout endpoint
     * 
     * @param refreshTokenRequest the refresh token request
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ResponseWrapper<String>> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        // Revoke refresh token
        refreshTokenService.revokeRefreshToken(refreshTokenRequest.getRefreshToken());
        
        // Clear security context
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new ResponseWrapper<>(true, 200, "Logged out successfully", null));
    }
}
