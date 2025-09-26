package com.obuspartners.modules.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Authentication Entry Point
 * Handles unauthorized access attempts
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Check for specific error messages set by ApiKeyAuthenticationFilter
        String errorMessage = (String) request.getAttribute("AUTH_ERROR_MESSAGE");
        String errorType = (String) request.getAttribute("AUTH_ERROR_TYPE");
        
        // Use specific message if available, otherwise use generic message
        if (errorMessage != null) {
            log.debug("Using specific auth error message: {} (type: {})", errorMessage, errorType);
        } else {
            errorMessage = "Full authentication is required to access this resource";
            errorType = "GENERIC_AUTH_REQUIRED";
        }

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", errorMessage);
        body.put("path", request.getRequestURI());
        
        // Add error type for debugging purposes
        if (errorType != null) {
            body.put("errorType", errorType);
        }

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
