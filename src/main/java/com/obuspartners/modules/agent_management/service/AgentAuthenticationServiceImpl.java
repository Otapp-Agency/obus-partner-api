package com.obuspartners.modules.agent_management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.obuspartners.modules.agent_management.domain.dto.AgentLoginRequestDto;
import com.obuspartners.modules.agent_management.domain.dto.AgentLoginResponseDto;
import com.obuspartners.modules.agent_management.domain.dto.AgentResponseDto;
import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.domain.enums.AgentStatus;
import com.obuspartners.modules.agent_management.repository.AgentRepository;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.auth_management.util.JwtUtil;
import com.obuspartners.modules.partner_management.service.PartnerApiKeyService;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of AgentAuthenticationService for agent authentication operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentAuthenticationServiceImpl implements AgentAuthenticationService {

    private final AgentRepository agentRepository;
    private final JwtUtil jwtUtil;
    private final PartnerApiKeyService partnerApiKeyService;
    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AgentLoginResponseDto authenticateAgent(AgentLoginRequestDto loginRequest) {
        log.info("Authenticating agent with agent number: {}", loginRequest.getAgentNumber());

        // Get API key and secret from request headers
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String apiKey = request.getHeader("X-API-Key");
        String apiSecret = request.getHeader("X-API-Secret");

        if (apiKey == null || apiSecret == null) {
            throw new ApiException("API key and secret are required", HttpStatus.UNAUTHORIZED);
        }

        // Authenticate partner using API key and secret
        Optional<com.obuspartners.modules.partner_management.service.PartnerApiKeyService.ApiKeyInfo> apiKeyInfoOpt = 
            partnerApiKeyService.validateApiKeyAndSecret(apiKey, apiSecret);
        
        if (apiKeyInfoOpt.isEmpty()) {
            throw new ApiException("Invalid API credentials", HttpStatus.UNAUTHORIZED);
        }

        com.obuspartners.modules.partner_management.service.PartnerApiKeyService.ApiKeyInfo apiKeyInfo = apiKeyInfoOpt.get();
        
        // Get partner using partnerUid
        Optional<Partner> partnerOpt = partnerRepository.findByUid(apiKeyInfo.getPartnerUid());
        if (partnerOpt.isEmpty()) {
            throw new ApiException("Partner not found", HttpStatus.UNAUTHORIZED);
        }
        
        Partner partner = partnerOpt.get();
        log.info("Partner authenticated: {} ({})", partner.getBusinessName(), partner.getCode());

        // Construct full username: PARTNERCODE-AGENTNUMBER
        String fullUsername = partner.getCode() + "-" + loginRequest.getAgentNumber();
        log.info("Constructed full username: {}", fullUsername);

        // Find agent by full username
        Optional<Agent> agentOpt = agentRepository.findByPassName(fullUsername);

        if (agentOpt.isEmpty()) {
            log.warn("Authentication failed for agent number: {} with partner: {}", loginRequest.getAgentNumber(), partner.getCode());
            throw new ApiException("Invalid login credentials", HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentOpt.get();

        // Verify agent belongs to the authenticated partner
        if (!agent.getPartner().getId().equals(partner.getId())) {
            throw new ApiException("Agent does not belong to the authenticated partner", HttpStatus.FORBIDDEN);
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassCode(), agent.getPassCode())) {
            throw new ApiException("Invalid login credentials", HttpStatus.UNAUTHORIZED);
        }

        // Check if agent is active
        if (agent.getStatus() != AgentStatus.ACTIVE) {
            log.warn("Agent {} is not active, status: {}", agent.getPassName(), agent.getStatus());
            //throw new ApiException("Agent account is not active", HttpStatus.FORBIDDEN);
        }

        // Update last activity
        agent.setLastActivityDate(LocalDateTime.now());
        agentRepository.save(agent);

        // Generate JWT tokens
        // Generate access token - limited scope if password change required
        String accessToken;
        if (agent.getUser() != null && agent.getUser().getRequirePasswordChange()) {
            accessToken = generateLimitedAgentAccessToken(agent);
        } else {
            accessToken = generateAgentAccessToken(agent);
        }
        
        // Generate refresh token only if password change is not required
        String refreshToken = null;
        if (agent.getUser() == null || !agent.getUser().getRequirePasswordChange()) {
            refreshToken = generateAgentRefreshToken(agent);
        }

        log.info("Agent {} authenticated successfully", agent.getPassName());

        return AgentLoginResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .type("Bearer")
            .passName(agent.getPassName())
            .partnerAgentNumber(agent.getPartnerAgentNumber())
            .partnerCode(agent.getPartner().getCode())
            .email(agent.getBusinessEmail())
            .userType("AGENT")
            .requireResetPassword(agent.getUser() != null ? agent.getUser().getRequirePasswordChange() : false)
            .partnerId(agent.getPartner().getId())
            .partnerUid(agent.getPartner().getUid())
            .partnerBusinessName(agent.getPartner().getBusinessName())
            // Essential frontend fields
            .displayName(agent.getUser() != null ? agent.getUser().getDisplayName() : agent.getContactPerson())
            .roles(agent.getUser() != null ? agent.getUser().getRoles().stream().map(role -> role.getRoleType().getValue()).toList() : java.util.List.of())
            .tokenExpiresAt(java.time.format.DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").format(jwtUtil.getExpirationDateFromToken(accessToken).toInstant().atZone(java.time.ZoneOffset.UTC).toLocalDateTime()))
            .agentId(agent.getId())
            .agentStatus(agent.getStatus().name())
            .lastLoginAt(agent.getLastActivityDate() != null ? java.time.format.DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss").format(agent.getLastActivityDate()) : null)
            .build();
    }

    @Override
    public boolean validateAgentToken(String token) {
        try {
            // Extract username from JWT token
            String username = jwtUtil.getUsernameFromToken(token);
            
            // Find agent by username
            Optional<Agent> agentOpt = agentRepository.findByPassName(username);
            
            if (agentOpt.isEmpty()) {
                return false;
            }
            
            Agent agent = agentOpt.get();
            
            // Create UserDetails for validation
            AgentUserDetails agentUserDetails = new AgentUserDetails(agent);
            
            // Validate token
            return jwtUtil.validateToken(token, agentUserDetails);
            
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public AgentResponseDto getAgentFromToken(String token) {
        try {
            // Extract username from JWT token
            String username = jwtUtil.getUsernameFromToken(token);
            
            // Find agent by username
            Optional<Agent> agentOpt = agentRepository.findByPassName(username);
            
            if (agentOpt.isPresent() && agentOpt.get().getStatus() == AgentStatus.ACTIVE) {
                return mapToAgentResponseDto(agentOpt.get());
            }
            
        } catch (Exception e) {
            log.debug("Failed to get agent from token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Generate JWT access token for agent
     * 
     * @param agent the agent entity
     * @return JWT access token
     */
    private String generateAgentAccessToken(Agent agent) {
        // Create a simple UserDetails-like object for JWT generation
        AgentUserDetails agentUserDetails = new AgentUserDetails(agent);
        return jwtUtil.generateToken(agentUserDetails);
    }

    private String generateLimitedAgentAccessToken(Agent agent) {
        // Create a simple UserDetails-like object for JWT generation with limited scope
        AgentUserDetails agentUserDetails = new AgentUserDetails(agent);
        return jwtUtil.generateLimitedToken(agentUserDetails, "password_change");
    }

    /**
     * Generate JWT refresh token for agent
     * 
     * @param agent the agent entity
     * @return JWT refresh token
     */
    private String generateAgentRefreshToken(Agent agent) {
        // Create a simple UserDetails-like object for JWT generation
        AgentUserDetails agentUserDetails = new AgentUserDetails(agent);
        return jwtUtil.generateRefreshToken(agentUserDetails);
    }

    /**
     * Simple UserDetails implementation for agents
     */
    private static class AgentUserDetails implements org.springframework.security.core.userdetails.UserDetails {
        private final Agent agent;

        public AgentUserDetails(Agent agent) {
            this.agent = agent;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_AGENT"));
        }

        @Override
        public String getPassword() {
            return agent.getPassCode();
        }

        @Override
        public String getUsername() {
            return agent.getPassName();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return agent.getStatus() == AgentStatus.ACTIVE;
        }
    }

    /**
     * Map Agent entity to AgentResponseDto
     * 
     * @param agent the agent entity
     * @return agent response DTO
     */
    private AgentResponseDto mapToAgentResponseDto(Agent agent) {
        AgentResponseDto dto = new AgentResponseDto();
        dto.setId(agent.getId());
        dto.setUid(agent.getUid());
        dto.setCode(agent.getCode());
        dto.setPartnerAgentNumber(agent.getPartnerAgentNumber());
        dto.setPassName(agent.getPassName());
        dto.setBusinessName(agent.getBusinessName());
        dto.setContactPerson(agent.getContactPerson());
        dto.setPhoneNumber(agent.getPhoneNumber());
        dto.setBusinessEmail(agent.getBusinessEmail());
        dto.setBusinessAddress(agent.getBusinessAddress());
        dto.setTaxId(agent.getTaxId());
        dto.setLicenseNumber(agent.getLicenseNumber());
        dto.setAgentType(agent.getAgentType());
        dto.setStatus(agent.getStatus());
        dto.setRegistrationDate(agent.getRegistrationDate());
        dto.setApprovalDate(agent.getApprovalDate());
        dto.setLastActivityDate(agent.getLastActivityDate());
        dto.setNotes(agent.getNotes());
        dto.setCreatedAt(agent.getCreatedAt());
        dto.setUpdatedAt(agent.getUpdatedAt());

        // Partner information
        if (agent.getPartner() != null) {
            dto.setPartnerId(agent.getPartner().getId());
            dto.setPartnerUid(agent.getPartner().getUid());
            dto.setPartnerCode(agent.getPartner().getCode());
            dto.setPartnerBusinessName(agent.getPartner().getBusinessName());
        }

        // Super agent information
        if (agent.getSuperAgent() != null) {
            dto.setSuperAgentId(agent.getSuperAgent().getId());
            dto.setSuperAgentUid(agent.getSuperAgent().getUid());
            dto.setSuperAgentCode(agent.getSuperAgent().getCode());
            dto.setSuperAgentBusinessName(agent.getSuperAgent().getBusinessName());
        }

        return dto;
    }
}
