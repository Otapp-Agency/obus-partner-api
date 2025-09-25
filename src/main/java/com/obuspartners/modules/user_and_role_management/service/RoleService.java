package com.obuspartners.modules.user_and_role_management.service;

import com.obuspartners.modules.user_and_role_management.domain.entity.Role;
import com.obuspartners.modules.user_and_role_management.domain.enums.RoleType;

import java.util.List;
import java.util.Optional;

/**
 * Role service interface
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
public interface RoleService {

    /**
     * Save a new role
     * 
     * @param role the role to save
     * @return the saved role
     */
    Role save(Role role);

    /**
     * Update an existing role
     * 
     * @param role the role to update
     * @return the updated role
     */
    Role update(Role role);

    /**
     * Find role by ID
     * 
     * @param id the role ID
     * @return Optional containing the role if found
     */
    Optional<Role> findById(Long id);

    /**
     * Find role by role type
     * 
     * @param roleType the role type
     * @return Optional containing the role if found
     */
    Optional<Role> findByRoleType(RoleType roleType);

    /**
     * Find all roles
     * 
     * @return list of all roles
     */
    List<Role> findAll();

    /**
     * Find all active roles
     * 
     * @return list of active roles
     */
    List<Role> findAllActive();

    /**
     * Find all inactive roles
     * 
     * @return list of inactive roles
     */
    List<Role> findAllInactive();

    /**
     * Check if role exists by role type
     * 
     * @param roleType the role type
     * @return true if role exists, false otherwise
     */
    boolean existsByRoleType(RoleType roleType);

    /**
     * Delete role by ID
     * 
     * @param id the role ID
     */
    void deleteById(Long id);

    /**
     * Set role active status
     * 
     * @param id the role ID
     * @param active true to activate, false to deactivate
     * @return the updated role
     */
    Role setRoleActive(Long id, boolean active);

    /**
     * Count roles by active status
     * 
     * @param active the active status
     * @return count of roles with the given active status
     */
    long countByActive(boolean active);
}
