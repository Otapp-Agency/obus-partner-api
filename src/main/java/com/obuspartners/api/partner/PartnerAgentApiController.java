package com.obuspartners.api.partner;

import com.obuspartners.modules.common.util.ResponseWrapper;
import com.obuspartners.modules.partner_integration.bmslg.Auth;
import com.obuspartners.modules.partner_integration.stations.StationService;
import com.obuspartners.modules.agent_management.service.AgentAuthenticationService;
import com.obuspartners.modules.agent_management.service.AgentBusCoreSystemService;
import com.obuspartners.modules.partner_management.service.PartnerService;
import com.obuspartners.modules.partner_management.service.PartnerApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Partner Agent API operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/partner/v1/agent-api")
@RequiredArgsConstructor
@Tag(name = "Partner Agent API", description = "Agent API operations for partners")
public class PartnerAgentApiController {

    private final AgentAuthenticationService agentAuthenticationService;
    private final AgentBusCoreSystemService agentBusCoreSystemService;
    private final PartnerService partnerService;
    private final PartnerApiKeyService partnerApiKeyService;
    private final StationService stationService;

    /**
     * Test endpoint for Partner Agent API - requires both partner API key and agent
     * JWT authentication
     * 
     * @return ResponseEntity with test response
     */
    @Operation(summary = "Test endpoint", description = "Simple test endpoint for Partner Agent API - requires both partner API key authentication and agent JWT authentication")
    @PostMapping("/test")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> test(HttpServletRequest request) {
        log.info("Test endpoint called for Partner Agent API");

        try {
            // Get authenticated agent information
            String authenticatedAgent = SecurityContextHolder.getContext().getAuthentication().getName();

            // Extract API Key and Secret for partner validation
            String apiKey = request.getHeader("X-API-Key");
            String apiSecret = request.getHeader("X-API-Secret");

            Map<String, Object> testData = new HashMap<>();
            testData.put("message", "Partner Agent API is working");
            testData.put("timestamp", String.valueOf(System.currentTimeMillis()));
            testData.put("endpoint", "/partner/v1/agent-api/test");
            testData.put("authenticatedAgent", authenticatedAgent);

            // Get agent information from JWT token
            try {
                String jwtToken = request.getHeader("Authorization");
                if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                    jwtToken = jwtToken.substring(7);
                    var agentResponse = agentAuthenticationService.getAgentFromToken(jwtToken);
                    if (agentResponse != null) {
                        Map<String, Object> agentInfo = new HashMap<>();
                        agentInfo.put("passName", agentResponse.getPassName());
                        agentInfo.put("agentId", agentResponse.getId());
                        agentInfo.put("status", agentResponse.getStatus());
                        agentInfo.put("businessName", agentResponse.getBusinessName());
                        agentInfo.put("contactPerson", agentResponse.getContactPerson());
                        agentInfo.put("phoneNumber", agentResponse.getPhoneNumber());
                        agentInfo.put("businessEmail", agentResponse.getBusinessEmail());
                        agentInfo.put("partnerId", agentResponse.getPartnerId());
                        agentInfo.put("partnerBusinessName", agentResponse.getPartnerBusinessName());
                        testData.put("agentInfo", agentInfo);

                        // Get agent's active bus core systems
                        try {
                            // For testing purposes, we'll create a simple agent entity
                            var agent = new com.obuspartners.modules.agent_management.domain.entity.Agent();
                            agent.setId(agentResponse.getId());
                            agent.setPassName(agentResponse.getPassName());

                            var activeBusSystems = agentBusCoreSystemService.getActiveBusCoreSystemsByAgent(agent);
                            if (activeBusSystems != null && !activeBusSystems.isEmpty()) {
                                testData.put("activeBusCoreSystems", activeBusSystems);
                            } else {
                                testData.put("activeBusCoreSystems", "No active bus core systems");
                            }
                        } catch (Exception e) {
                            log.warn("Could not fetch bus core systems for agent: {}", authenticatedAgent, e);
                            testData.put("activeBusCoreSystems", "Not available");
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch agent information for: {}", authenticatedAgent, e);
                testData.put("agentInfo", "Not available");
            }

            // Get partner information using API key
            try {
                if (apiKey != null && apiSecret != null) {
                    Optional<PartnerApiKeyService.ApiKeyInfo> apiKeyInfoOpt = partnerApiKeyService
                            .validateApiKeyAndSecret(apiKey, apiSecret);
                    if (apiKeyInfoOpt.isPresent()) {
                        var apiKeyInfo = apiKeyInfoOpt.get();
                        Optional<com.obuspartners.modules.partner_management.domain.dto.PartnerResponseDto> partnerOpt = partnerService
                                .getPartnerByUid(apiKeyInfo.getPartnerUid());

                        if (partnerOpt.isPresent()) {
                            var partnerInfo = partnerOpt.get();
                            Map<String, Object> partnerData = new HashMap<>();
                            partnerData.put("partnerId", partnerInfo.getId());
                            partnerData.put("partnerUid", partnerInfo.getUid());
                            partnerData.put("businessName", partnerInfo.getBusinessName());
                            partnerData.put("legalName", partnerInfo.getLegalName());
                            partnerData.put("email", partnerInfo.getEmail());
                            partnerData.put("phoneNumber", partnerInfo.getPhoneNumber());
                            partnerData.put("isActive", partnerInfo.getIsActive());
                            partnerData.put("isVerified", partnerInfo.getIsVerified());
                            partnerData.put("tier", partnerInfo.getTier());
                            partnerData.put("type", partnerInfo.getType());
                            partnerData.put("status", partnerInfo.getStatus());
                            partnerData.put("code", partnerInfo.getCode());
                            testData.put("partnerInfo", partnerData);

                            // Add API key info
                            Map<String, Object> apiKeyData = new HashMap<>();
                            apiKeyData.put("keyName", apiKeyInfo.getKeyName());
                            apiKeyData.put("environment", apiKeyInfo.getEnvironment());
                            apiKeyData.put("permissions", apiKeyInfo.getPermissions());
                            apiKeyData.put("expiresAt", apiKeyInfo.getExpiresAt());
                            apiKeyData.put("isPrimary", apiKeyInfo.isPrimary());
                            testData.put("apiKeyInfo", apiKeyData);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch partner information for API key: {}", apiKey, e);
                testData.put("partnerInfo", "Not available");
            }

            ResponseWrapper<Map<String, Object>> response = new ResponseWrapper<>(
                    true,
                    200,
                    "Test successful",
                    testData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in test endpoint", e);
            ResponseWrapper<Map<String, Object>> errorResponse = new ResponseWrapper<>(
                    false,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal server error",
                    null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Test BMSLG Login", description = "Test BMSLG authentication with agent credentials - requires both partner API key authentication and agent JWT authentication")
    @PostMapping("/test-bms-login")
    public ResponseEntity<ResponseWrapper<Auth.AuthResponse>> testBmsLogin(@RequestBody BmsAuthInfo info,
            HttpServletRequest request) {
        log.info("BMSLG test login endpoint called for username: {}", info.getUsername());

        try {
            // Perform BMSLG authentication
            Auth.AuthResponse authResponse = (new Auth()).authenticateWithDefaults(
                    info.getUsername(),
                    info.getPassword(),
                    info.getOwnerId());

            // Wrap response in standard format
            ResponseWrapper<Auth.AuthResponse> response = new ResponseWrapper<>(
                    authResponse.isSuccess(),
                    authResponse.getHttpCode(),
                    authResponse.isSuccess() ? "BMSLG authentication successful" : "BMSLG authentication failed",
                    authResponse);

            return ResponseEntity.status(authResponse.getHttpCode()).body(response);

        } catch (Exception e) {
            log.error("Error during BMSLG authentication test", e);
            ResponseWrapper<Auth.AuthResponse> errorResponse = new ResponseWrapper<>(
                    false,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal server error during BMSLG authentication",
                    null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Fetch all stations from BMSLG - requires both partner API key and agent JWT
     * authentication
     *
     * @return ResponseEntity with stations data
     */
    @Operation(summary = "Fetch All Stations", description = "Fetch all stations from BMSLG system - requires both partner API key authentication and agent JWT authentication")
    @GetMapping("/stations")
    public ResponseEntity<Object> fetchAllStations(HttpServletRequest request) {
        log.info("Fetch all stations endpoint called for Partner Agent API");
        Object stationsData = stationService.fetchAllStations();
        return ResponseEntity.ok(stationsData);
    }

    @Data
    static class BmsAuthInfo {
        String username;
        String password;
        String ownerId;
    }

}
