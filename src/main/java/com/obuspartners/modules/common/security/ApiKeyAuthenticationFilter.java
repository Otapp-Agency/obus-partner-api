package com.obuspartners.modules.common.security;

import com.obuspartners.modules.partner_management.service.PartnerApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * API Key Authentication Filter
 * Validates API key and secret for partner authentication
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private PartnerApiKeyService partnerApiKeyService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");
        String apiSecret = request.getHeader("X-API-Secret");

        log.debug("API Key Filter - Request URI: {}, API Key: {}, API Secret: {}", 
                 request.getRequestURI(), apiKey, apiSecret != null ? "***" : "null");

        if (apiKey != null && apiSecret != null) {
            try {
                // Validate the API key and secret using the service
                Optional<PartnerApiKeyService.ApiKeyInfo> apiKeyInfoOpt = 
                    partnerApiKeyService.validateApiKeyAndSecret(apiKey, apiSecret);
                
                if (apiKeyInfoOpt.isPresent()) {
                    PartnerApiKeyService.ApiKeyInfo apiKeyInfo = apiKeyInfoOpt.get();
                    
                    // Record usage
                    partnerApiKeyService.recordApiKeyUsage(apiKey);
                    
                    // Create authentication token with partner UID (no roles needed for API key auth)
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            apiKeyInfo.getPartnerUid(),
                            null,
                            Collections.emptyList() // No roles - just authenticated
                        );
                    
                    // Add API key info as details for later use
                    authToken.setDetails(apiKeyInfo);
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("API key authentication successful for partner: {} with key: {}", 
                             apiKeyInfo.getPartnerUid(), apiKeyInfo.getKeyName());
                    
                } else {
                    log.warn("Invalid API key or secret: {}", apiKey);
                    // Set specific error message for invalid credentials
                    request.setAttribute("AUTH_ERROR_MESSAGE", "Invalid API key or secret provided");
                    request.setAttribute("AUTH_ERROR_TYPE", "INVALID_CREDENTIALS");
                }
                
            } catch (Exception e) {
                log.error("Error during API key authentication: {}", e.getMessage());
                // Set specific error message for authentication errors
                request.setAttribute("AUTH_ERROR_MESSAGE", "API key authentication failed: " + e.getMessage());
                request.setAttribute("AUTH_ERROR_TYPE", "AUTH_ERROR");
            }
        } else {
            log.debug("No API key or secret provided in headers");
            // Set specific error message for missing credentials
            request.setAttribute("AUTH_ERROR_MESSAGE", "API key and secret are required");
            request.setAttribute("AUTH_ERROR_TYPE", "MISSING_CREDENTIALS");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Only apply to partner API endpoints (check both with and without context path)
        String requestURI = request.getRequestURI();
        boolean shouldNotFilter = !requestURI.startsWith("/partner/") && !requestURI.startsWith("/api/partner/");
        log.debug("API Key Filter shouldNotFilter: {} for URI: {}", shouldNotFilter, requestURI);
        return shouldNotFilter;
    }
}
