package com.obuspartners.modules.user_and_role_management.repository;

import com.obuspartners.modules.user_and_role_management.domain.entity.SystemUser;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserStatus;
import com.obuspartners.modules.user_and_role_management.domain.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SystemUser entity
 * Provides data access methods for SystemUser operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {

    /**
     * Find system user by UID
     * 
     * @param uid the UID
     * @return Optional containing the system user if found
     */
    Optional<SystemUser> findByUid(String uid);

    /**
     * Find system user by UID with user and partner information
     * Uses JOIN FETCH to eagerly load User and Partner entities
     * 
     * @param uid the UID
     * @return Optional containing the system user with loaded relationships
     */
    @Query("SELECT su FROM SystemUser su " +
           "LEFT JOIN FETCH su.user u " +
           "LEFT JOIN FETCH u.partner p " +
           "WHERE su.uid = :uid")
    Optional<SystemUser> findByUidWithUserAndPartner(@Param("uid") String uid);

    /**
     * Find system user by user ID
     * 
     * @param userId the user ID
     * @return Optional containing the system user if found
     */
    Optional<SystemUser> findByUserId(Long userId);

    /**
     * Find system user by username
     * 
     * @param username the username
     * @return Optional containing the system user if found
     */
    Optional<SystemUser> findByUserUsername(String username);

    /**
     * Find system user by employee ID
     * 
     * @param employeeId the employee ID
     * @return Optional containing the system user if found
     */
    Optional<SystemUser> findByEmployeeId(String employeeId);

    /**
     * Find system user by personal email or work email
     * 
     * @param personalEmail the personal email
     * @param workEmail the work email
     * @return Optional containing the system user if found
     */
    @Query("SELECT su FROM SystemUser su WHERE su.personalEmail = :personalEmail OR su.workEmail = :workEmail")
    Optional<SystemUser> findByPersonalEmailOrWorkEmail(@Param("personalEmail") String personalEmail, 
                                                        @Param("workEmail") String workEmail);

    /**
     * Find system users by user type
     * 
     * @param userType the user type
     * @return list of system users with the specified user type
     */
    List<SystemUser> findByUserUserType(UserType userType);

    /**
     * Find system users by status
     * 
     * @param status the user status
     * @return list of system users with the specified status
     */
    List<SystemUser> findByStatus(UserStatus status);

    /**
     * Find system users by department
     * 
     * @param department the department
     * @return list of system users in the specified department
     */
    List<SystemUser> findByDepartment(String department);

    /**
     * Check if UID exists
     * 
     * @param uid the UID
     * @return true if UID exists, false otherwise
     */
    boolean existsByUid(String uid);

    /**
     * Check if employee ID exists
     * 
     * @param employeeId the employee ID
     * @return true if employee ID exists, false otherwise
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * Check if personal email exists
     * 
     * @param personalEmail the personal email
     * @return true if personal email exists, false otherwise
     */
    boolean existsByPersonalEmail(String personalEmail);

    /**
     * Check if work email exists
     * 
     * @param workEmail the work email
     * @return true if work email exists, false otherwise
     */
    boolean existsByWorkEmail(String workEmail);

    /**
     * Count system users by user type
     * 
     * @param userType the user type
     * @return count of system users with the specified user type
     */
    long countByUserUserType(UserType userType);

    /**
     * Count system users by status
     * 
     * @param status the user status
     * @return count of system users with the specified status
     */
    long countByStatus(UserStatus status);

    /**
     * Count system users by department
     * 
     * @param department the department
     * @return count of system users in the specified department
     */
    long countByDepartment(String department);

    /**
     * Find all system users with user and partner information (for pagination)
     * Uses JOIN FETCH to eagerly load User and Partner entities
     * 
     * @param pageable pagination information
     * @return Page of system users with loaded relationships
     */
    @Query("SELECT DISTINCT su FROM SystemUser su " +
           "LEFT JOIN FETCH su.user u " +
           "LEFT JOIN FETCH u.partner p")
    Page<SystemUser> findAllWithUserAndPartner(Pageable pageable);
}
