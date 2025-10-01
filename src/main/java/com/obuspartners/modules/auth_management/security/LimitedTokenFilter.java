package com.obuspartners.modules.auth_management.security;

import com.obuspartners.modules.auth_management.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to handle limited scope tokens and restrict access to normal operations
 * when user requires password change
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LimitedTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Allowed endpoints for password change scope
    private static final String[] PASSWORD_CHANGE_ALLOWED_PATHS = {
        "/v1/auth/password/change",
        "/api/v1/auth/password/change",
        "/api/admin/v1/users/change-password",
        "/v1/auth/logout",
        "/api/v1/auth/logout"
    };

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        // Extract token from header
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                log.debug("Unable to get JWT Token or JWT Token has expired");
            }
        }

        // Check if token is limited scope
        if (username != null && jwtUtil.isLimitedToken(jwtToken)) {
            String tokenScope = jwtUtil.getTokenScope(jwtToken);
            
            // Block access to non-allowed endpoints for limited tokens
            if (!isAllowedForScope(request.getRequestURI(), tokenScope)) {
                log.warn("Blocked access to {} for limited token with scope: {}", 
                        request.getRequestURI(), tokenScope);
                
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"status\":false,\"statusCode\":403,\"message\":\"Access denied. Password change required.\",\"data\":null}"
                );
                return;
            }
        }

        // Continue with normal authentication flow
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the requested path is allowed for the given token scope
     */
    private boolean isAllowedForScope(String requestPath, String tokenScope) {
        if ("password_change".equals(tokenScope)) {
            for (String allowedPath : PASSWORD_CHANGE_ALLOWED_PATHS) {
                if (requestPath.startsWith(allowedPath)) {
                    return true;
                }
            }
            return false;
        }
        
        // Non-limited tokens have full access
        return true;
    }
}
