package com.obuspartners.modules.user_and_role_management.service;

import com.obuspartners.modules.user_and_role_management.domain.entity.Role;
import com.obuspartners.modules.user_and_role_management.domain.enums.RoleType;
import com.obuspartners.modules.user_and_role_management.repository.RoleRepository;
import com.obuspartners.modules.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Role service implementation
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role save(Role role) {
        log.debug("Saving role: {}", role.getRoleType());
        
        // Check if role type already exists
        if (roleRepository.existsByRoleType(role.getRoleType())) {
            throw new ApiException("Role with type '" + role.getRoleType() + "' already exists", HttpStatus.CONFLICT);
        }
        
        Role savedRole = roleRepository.save(role);
        log.info("Role saved successfully with ID: {}", savedRole.getId());
        return savedRole;
    }

    @Override
    public Role update(Role role) {
        log.debug("Updating role: {}", role.getRoleType());
        
        // Check if role exists
        if (!roleRepository.existsById(role.getId())) {
            throw new ApiException("Role not found with ID: " + role.getId(), HttpStatus.NOT_FOUND);
        }
        
        // Check if another role with the same type exists (excluding current role)
        Optional<Role> existingRole = roleRepository.findByRoleType(role.getRoleType());
        if (existingRole.isPresent() && !existingRole.get().getId().equals(role.getId())) {
            throw new ApiException("Role with type '" + role.getRoleType() + "' already exists", HttpStatus.CONFLICT);
        }
        
        Role updatedRole = roleRepository.save(role);
        log.info("Role updated successfully with ID: {}", updatedRole.getId());
        return updatedRole;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        log.debug("Finding role by ID: {}", id);
        return roleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByRoleType(RoleType roleType) {
        log.debug("Finding role by type: {}", roleType);
        return roleRepository.findByRoleType(roleType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        log.debug("Finding all roles");
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAllActive() {
        log.debug("Finding all active roles");
        return roleRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAllInactive() {
        log.debug("Finding all inactive roles");
        return roleRepository.findByActiveFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoleType(RoleType roleType) {
        log.debug("Checking if role exists by type: {}", roleType);
        return roleRepository.existsByRoleType(roleType);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting role by ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Role not found with ID: " + id, HttpStatus.NOT_FOUND));
        
        // Check if role is assigned to any users
        if (!role.getUsers().isEmpty()) {
            throw new ApiException("Cannot delete role '" + role.getRoleType() + "' as it is assigned to " + role.getUsers().size() + " user(s)", HttpStatus.BAD_REQUEST);
        }
        
        roleRepository.deleteById(id);
        log.info("Role deleted successfully with ID: {}", id);
    }

    @Override
    public Role setRoleActive(Long id, boolean active) {
        log.debug("Setting role active status: {} for role ID: {}", active, id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Role not found with ID: " + id, HttpStatus.NOT_FOUND));
        role.setActive(active);
        Role updatedRole = roleRepository.save(role);
        log.info("Role active status updated to: {} for role ID: {}", active, id);
        return updatedRole;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByActive(boolean active) {
        log.debug("Counting roles by active status: {}", active);
        return roleRepository.countByActive(active);
    }
}
