package com.obus.partner.api.auth;

import com.obus.partner.modules.auth_management.domain.dto.AuthResponse;
import com.obus.partner.modules.auth_management.domain.dto.LoginRequest;
import com.obus.partner.modules.auth_management.util.JwtUtil;
import com.obus.partner.modules.common.exception.UserAlreadyExistsException;
import com.obus.partner.modules.user_and_role_management.domain.entity.Role;
import com.obus.partner.modules.user_and_role_management.domain.entity.User;
import com.obus.partner.modules.user_and_role_management.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for login and registration
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Login endpoint
     * 
     * @param loginRequest the login request
     * @return authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());

            User user = userService.findByUsername(loginRequest.getUsername()).orElseThrow();
            
            AuthResponse response = new AuthResponse(
                jwt,
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

    /**
     * Register endpoint
     * 
     * @param user the user to register
     * @return success message
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                throw new UserAlreadyExistsException("Username is already taken!");
            }

            if (userService.existsByEmail(user.getEmail())) {
                throw new UserAlreadyExistsException("Email is already in use!");
            }

            user.setRole(Role.USER);
            userService.save(user);

            return ResponseEntity.ok("User registered successfully");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
}
