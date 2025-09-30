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

        // Skip JWT processing if authentication is already set
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
        } else {
            log.debug("No JWT Token found in request");
        }

        if (jwtToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Validate token and get agent
            if (agentAuthenticationService.validateAgentToken(jwtToken)) {
                var agentResponse = agentAuthenticationService.getAgentFromToken(jwtToken);
                
                if (agentResponse != null) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                        new UsernamePasswordAuthenticationToken(
                            agentResponse.getPassName(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_AGENT")));
                    
                    usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    
                    log.debug("Agent JWT authentication successful for: {}", agentResponse.getPassName());
                }
            }
        }
        
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Only apply to agent API endpoints
        String requestURI = request.getRequestURI();
        return !requestURI.startsWith("/api/agent/");
    }
}
