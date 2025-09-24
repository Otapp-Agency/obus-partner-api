package com.obuspartners.modules.user_and_role_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.obuspartners.modules.user_and_role_management.domain.entity.User;

import java.util.Optional;

/**
 * User repository interface
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * 
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * 
     * @param email the email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     * 
     * @param username the username
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * 
     * @param email the email
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
