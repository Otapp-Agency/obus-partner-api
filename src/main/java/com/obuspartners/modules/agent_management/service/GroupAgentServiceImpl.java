package com.obuspartners.modules.agent_management.service;

import com.obuspartners.modules.agent_management.domain.entity.GroupAgent;
import com.obuspartners.modules.agent_management.domain.enums.GroupAgentStatus;
import com.obuspartners.modules.agent_management.domain.enums.GroupAgentType;
import com.obuspartners.modules.agent_management.repository.GroupAgentRepository;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for GroupAgent management operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupAgentServiceImpl implements GroupAgentService {

    private final GroupAgentRepository groupAgentRepository;
    private final PartnerRepository partnerRepository;

    @Override
    public GroupAgent createGroupAgent(GroupAgent groupAgent) {
        log.info("Creating new group agent: {}", groupAgent.getName());
        
        // Validate the group agent
        if (!validateGroupAgent(groupAgent)) {
            throw new RuntimeException("Invalid group agent data");
        }

        // Check if code already exists for partner
        if (existsByPartnerAndCode(groupAgent.getPartner(), groupAgent.getCode())) {
            throw new RuntimeException("Group agent code already exists for this partner: " + groupAgent.getCode());
        }

        // Check if external system identifier already exists for partner
        if (existsByPartnerAndExternalSystemIdentifier(groupAgent.getPartner(), groupAgent.getExternalSystemIdentifier())) {
            throw new RuntimeException("External system identifier already exists for this partner: " + groupAgent.getExternalSystemIdentifier());
        }

        // Set default values
        if (groupAgent.getStatus() == null) {
            groupAgent.setStatus(GroupAgentStatus.ACTIVE);
        }
        if (groupAgent.getType() == null) {
            groupAgent.setType(GroupAgentType.STANDARD);
        }

        GroupAgent savedGroupAgent = groupAgentRepository.save(groupAgent);
        log.info("Successfully created group agent with ID: {}", savedGroupAgent.getId());
        
        return savedGroupAgent;
    }

    @Override
    public GroupAgent createGroupAgent(Partner partner, String name, String code, String externalSystemIdentifier, GroupAgentType type) {
        log.info("Creating new group agent for partner {}: {}", partner.getCode(), name);
        
        GroupAgent groupAgent = new GroupAgent();
        groupAgent.setPartner(partner);
        groupAgent.setName(name);
        groupAgent.setCode(code);
        groupAgent.setExternalSystemIdentifier(externalSystemIdentifier);
        groupAgent.setType(type);
        groupAgent.setStatus(GroupAgentStatus.ACTIVE);
        
        return createGroupAgent(groupAgent);
    }

    @Override
    public GroupAgent updateGroupAgent(GroupAgent groupAgent) {
        log.info("Updating group agent: {}", groupAgent.getName());
        
        // Validate the group agent
        if (!validateGroupAgent(groupAgent)) {
            throw new RuntimeException("Invalid group agent data");
        }

        // Check if group agent exists
        GroupAgent existingGroupAgent = groupAgentRepository.findById(groupAgent.getId())
                .orElseThrow(() -> new RuntimeException("Group agent not found with ID: " + groupAgent.getId()));

        // Check if code already exists for another group agent in the same partner
        if (!existingGroupAgent.getCode().equals(groupAgent.getCode()) && 
            existsByPartnerAndCode(groupAgent.getPartner(), groupAgent.getCode())) {
            throw new RuntimeException("Group agent code already exists for this partner: " + groupAgent.getCode());
        }

        // Check if external system identifier already exists for another group agent in the same partner
        if (!existingGroupAgent.getExternalSystemIdentifier().equals(groupAgent.getExternalSystemIdentifier()) && 
            existsByPartnerAndExternalSystemIdentifier(groupAgent.getPartner(), groupAgent.getExternalSystemIdentifier())) {
            throw new RuntimeException("External system identifier already exists for this partner: " + groupAgent.getExternalSystemIdentifier());
        }

        GroupAgent updatedGroupAgent = groupAgentRepository.save(groupAgent);
        log.info("Successfully updated group agent with ID: {}", updatedGroupAgent.getId());
        
        return updatedGroupAgent;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupAgent> getGroupAgentById(Long id) {
        log.debug("Finding group agent by ID: {}", id);
        return groupAgentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupAgent> getGroupAgentByUid(String uid) {
        log.debug("Finding group agent by UID: {}", uid);
        return groupAgentRepository.findByUid(uid);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupAgent> getGroupAgentByPartnerAndCode(Partner partner, String code) {
        log.debug("Finding group agent by partner {} and code: {}", partner.getCode(), code);
        return groupAgentRepository.findByPartnerAndCode(partner, code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgent> getGroupAgentsByPartner(Partner partner) {
        log.debug("Finding all group agents for partner: {}", partner.getCode());
        return groupAgentRepository.findByPartner(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupAgent> getGroupAgentsByPartner(Partner partner, Pageable pageable) {
        log.debug("Finding group agents for partner {} with pagination", partner.getCode());
        return groupAgentRepository.findByPartner(partner, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgent> getGroupAgentsByPartnerUid(String partnerUid) {
        log.debug("Finding all group agents for partner UID: {}", partnerUid);
        return groupAgentRepository.findByPartnerUid(partnerUid);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupAgent> getGroupAgentsByPartnerUid(String partnerUid, Pageable pageable) {
        log.debug("Finding group agents for partner UID {} with pagination", partnerUid);
        return groupAgentRepository.findByPartnerUid(partnerUid, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgent> getGroupAgentsByStatus(GroupAgentStatus status) {
        log.debug("Finding all group agents with status: {}", status);
        return groupAgentRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgent> getGroupAgentsByType(GroupAgentType type) {
        log.debug("Finding all group agents with type: {}", type);
        return groupAgentRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgent> getActiveGroupAgentsByPartner(Partner partner) {
        log.debug("Finding active group agents for partner: {}", partner.getCode());
        return groupAgentRepository.findActiveByPartner(partner);
    }

    @Override
    public GroupAgent activateGroupAgent(GroupAgent groupAgent) {
        log.info("Activating group agent: {}", groupAgent.getName());
        groupAgent.activate();
        return groupAgentRepository.save(groupAgent);
    }

    @Override
    public GroupAgent suspendGroupAgent(GroupAgent groupAgent) {
        log.info("Suspending group agent: {}", groupAgent.getName());
        groupAgent.suspend();
        return groupAgentRepository.save(groupAgent);
    }

    @Override
    public GroupAgent deactivateGroupAgent(GroupAgent groupAgent) {
        log.info("Deactivating group agent: {}", groupAgent.getName());
        groupAgent.deactivate();
        return groupAgentRepository.save(groupAgent);
    }

    @Override
    public void deleteGroupAgent(Long id) {
        log.info("Deleting group agent with ID: {}", id);
        
        GroupAgent groupAgent = groupAgentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group agent not found with ID: " + id));

        if (!canDeleteGroupAgent(groupAgent)) {
            throw new RuntimeException("Cannot delete group agent. It has associated agents or bus core systems.");
        }

        groupAgentRepository.delete(groupAgent);
        log.info("Successfully deleted group agent with ID: {}", id);
    }

    @Override
    public void deleteGroupAgentByUid(String uid) {
        log.info("Deleting group agent with UID: {}", uid);
        
        GroupAgent groupAgent = groupAgentRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Group agent not found with UID: " + uid));

        if (!canDeleteGroupAgent(groupAgent)) {
            throw new RuntimeException("Cannot delete group agent. It has associated agents or bus core systems.");
        }

        groupAgentRepository.delete(groupAgent);
        log.info("Successfully deleted group agent with UID: {}", uid);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPartnerAndCode(Partner partner, String code) {
        return groupAgentRepository.existsByPartnerAndCode(partner, code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPartnerAndExternalSystemIdentifier(Partner partner, String externalSystemIdentifier) {
        return groupAgentRepository.existsByPartnerAndExternalSystemIdentifier(partner, externalSystemIdentifier);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByPartner(Partner partner) {
        return groupAgentRepository.countByPartner(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByPartner(Partner partner) {
        return groupAgentRepository.countActiveByPartner(partner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgent> searchGroupAgentsByPartner(Partner partner, String searchTerm) {
        log.debug("Searching group agents for partner {} with term: {}", partner.getCode(), searchTerm);
        return groupAgentRepository.findByPartnerAndSearchTerm(partner, searchTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupAgent> searchGroupAgentsByPartner(Partner partner, String searchTerm, Pageable pageable) {
        log.debug("Searching group agents for partner {} with term {} and pagination", partner.getCode(), searchTerm);
        return groupAgentRepository.findByPartnerAndSearchTerm(partner, searchTerm, pageable);
    }

    @Override
    public void updateLastActivity(GroupAgent groupAgent) {
        groupAgent.updateLastActivity();
        groupAgentRepository.save(groupAgent);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateGroupAgent(GroupAgent groupAgent) {
        if (groupAgent == null) {
            return false;
        }
        
        if (groupAgent.getPartner() == null) {
            log.warn("Group agent validation failed: Partner is required");
            return false;
        }
        
        if (groupAgent.getName() == null || groupAgent.getName().trim().isEmpty()) {
            log.warn("Group agent validation failed: Name is required");
            return false;
        }
        
        if (groupAgent.getCode() == null || groupAgent.getCode().trim().isEmpty()) {
            log.warn("Group agent validation failed: Code is required");
            return false;
        }
        
        if (groupAgent.getExternalSystemIdentifier() == null || groupAgent.getExternalSystemIdentifier().trim().isEmpty()) {
            log.warn("Group agent validation failed: External system identifier is required");
            return false;
        }
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteGroupAgent(GroupAgent groupAgent) {
        // Check if group agent has associated agents
        if (groupAgent.hasAgents()) {
            log.warn("Cannot delete group agent {}: has associated agents", groupAgent.getName());
            return false;
        }
        
        // Check if group agent has associated bus core systems
        if (groupAgent.hasCoreBusSystems()) {
            log.warn("Cannot delete group agent {}: has associated bus core systems", groupAgent.getName());
            return false;
        }
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgent> getGroupAgentsWithRecentActivity(LocalDateTime since) {
        log.debug("Finding group agents with recent activity since: {}", since);
        return groupAgentRepository.findWithRecentActivity(since);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupAgent> getAllGroupAgents(Pageable pageable) {
        log.debug("Finding all group agents with pagination: {}", pageable);
        return groupAgentRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupAgent> searchGroupAgents(String searchTerm, Pageable pageable) {
        log.debug("Searching group agents with term: {} and pagination: {}", searchTerm, pageable);
        return groupAgentRepository.searchGroupAgents(searchTerm, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupAgent> getAllActiveGroupAgents() {
        log.debug("Finding all active group agents for assignment");
        return groupAgentRepository.findByStatus(GroupAgentStatus.ACTIVE);
    }
}
