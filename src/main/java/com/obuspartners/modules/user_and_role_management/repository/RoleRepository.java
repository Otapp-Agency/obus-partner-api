package com.obuspartners.modules.user_and_role_management.repository;

import com.obuspartners.modules.user_and_role_management.domain.entity.Role;
import com.obuspartners.modules.user_and_role_management.domain.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Role repository interface
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by role type
     * 
     * @param roleType the role type
     * @return Optional containing the role if found
     */
    Optional<Role> findByRoleType(RoleType roleType);

    /**
     * Check if role exists by role type
     * 
     * @param roleType the role type
     * @return true if role exists, false otherwise
     */
    boolean existsByRoleType(RoleType roleType);

    /**
     * Find all active roles
     * 
     * @return list of active roles
     */
    List<Role> findByActiveTrue();

    /**
     * Find all inactive roles
     * 
     * @return list of inactive roles
     */
    List<Role> findByActiveFalse();

    /**
     * Find roles by display name containing the given text
     * 
     * @param displayName the display name text to search
     * @return list of roles matching the criteria
     */
    List<Role> findByDisplayNameContainingIgnoreCase(String displayName);

    /**
     * Find roles by description containing the given text
     * 
     * @param description the description text to search
     * @return list of roles matching the criteria
     */
    List<Role> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Find roles assigned to a specific user
     * 
     * @param userId the user ID
     * @return list of roles assigned to the user
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * Count roles by active status
     * 
     * @param active the active status
     * @return count of roles with the given active status
     */
    long countByActive(boolean active);
}
