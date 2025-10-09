package com.obuspartners.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.obuspartners.modules.agent_management.domain.dto.AgentLoginRequestDto;
import com.obuspartners.modules.agent_management.domain.dto.AgentLoginResponseDto;
import com.obuspartners.modules.agent_management.service.AgentAuthenticationService;
import com.obuspartners.modules.common.util.ResponseWrapper;

/**
 * Controller for agent authentication operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth/agent")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication APIs - Login, Register, Refresh Token, Agent Auth")
public class AgentAuthController {

    private final AgentAuthenticationService agentAuthenticationService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate agent", description = "Authenticate agent using agent number and password. Requires X-API-Key and X-API-Secret headers for partner authentication.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or API credentials"),
        @ApiResponse(responseCode = "403", description = "Agent account not active or agent doesn't belong to partner"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<ResponseWrapper<AgentLoginResponseDto>> login(
            @Valid @RequestBody AgentLoginRequestDto loginRequest) {

        log.info("Agent login attempt for agent number: {}", loginRequest.getAgentNumber());

        try {
            AgentLoginResponseDto response = agentAuthenticationService.authenticateAgent(loginRequest);
            
            return ResponseEntity.ok()
                .body(new ResponseWrapper<>(true, 200, "Login successful", response));

        } catch (Exception e) {
            log.error("Agent authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, 401, "Authentication failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate agent token", description = "Validate agent authentication token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "401", description = "Token is invalid")
    })
    public ResponseEntity<ResponseWrapper<Boolean>> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.replace("Bearer ", "");
            boolean isValid = agentAuthenticationService.validateAgentToken(token);
            
            if (isValid) {
                return ResponseEntity.ok()
                    .body(new ResponseWrapper<>(true, 200, "Token is valid", true));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseWrapper<>(false, 401, "Token is invalid", false));
            }

        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, 401, "Token validation failed", false));
        }
    }
}
