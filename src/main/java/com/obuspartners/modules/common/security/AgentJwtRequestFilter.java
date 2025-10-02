package com.obuspartners.modules.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.obuspartners.modules.agent_management.service.AgentAuthenticationService;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Request Filter for Agent authentication
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentJwtRequestFilter extends OncePerRequestFilter {

    private final AgentAuthenticationService agentAuthenticationService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        log.info("AgentJwtRequestFilter processing request: {}", request.getRequestURI());

        // Skip JWT processing if authentication is already set (by other filters)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.info("Authentication already set, skipping AgentJwtRequestFilter");
            chain.doFilter(request, response);
            return;
        }

        // Extract API Key and Secret for partner validation
        final String apiKey = request.getHeader("X-API-Key");
        final String apiSecret = request.getHeader("X-API-Secret");
        
        log.info("API Key present: {}, API Secret present: {}", 
            apiKey != null, apiSecret != null);

        // Extract JWT token for agent authentication
        final String requestTokenHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", requestTokenHeader != null ? "Bearer ***" : "null");

        String jwtToken = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            log.info("JWT token extracted, length: {}", jwtToken.length());
        } else {
            log.info("No JWT Token found in request");
        }

        // Both API key/secret and JWT token are required
        if (apiKey == null || apiSecret == null) {
            log.info("Missing API Key or Secret");
            chain.doFilter(request, response);
            return;
        }

        if (jwtToken == null) {
            log.info("Missing JWT Token");
            chain.doFilter(request, response);
            return;
        }

        // TODO: Validate API Key and Secret here (partner validation)
        // For now, we'll assume they're valid and proceed with JWT validation
        log.info("API Key and Secret validation - TODO: implement partner validation");

        // Validate JWT token and authenticate agent
        log.info("Validating agent token...");
        if (agentAuthenticationService.validateAgentToken(jwtToken)) {
            log.info("Agent token validation successful");
            var agentResponse = agentAuthenticationService.getAgentFromToken(jwtToken);
            
            if (agentResponse != null) {
                log.info("Agent found: {}", agentResponse.getPassName());
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                    new UsernamePasswordAuthenticationToken(
                        agentResponse.getPassName(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_AGENT")));
                
                usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                
                log.info("Agent JWT authentication successful for: {}", agentResponse.getPassName());
            } else {
                log.info("Agent response is null");
            }
        } else {
            log.info("Agent token validation failed");
        }
        
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Apply to agent API endpoints (with and without context path)
        String requestURI = request.getRequestURI();
        boolean shouldNotFilter = !requestURI.startsWith("/api/agent/") && 
                                 !requestURI.startsWith("/partner/v1/agent-api/") &&
                                 !requestURI.startsWith("/api/partner/v1/agent-api/");
        
        // Skip register-self endpoint as it only needs API key validation, not JWT authentication
        if (requestURI.contains("/agents/register-self")) {
            shouldNotFilter = true;
        }
        
        log.info("AgentJwtRequestFilter shouldNotFilter: {} for URI: {}", shouldNotFilter, requestURI);
        return shouldNotFilter;
    }
}
