package com.obuspartners.modules.bus_core_system.repository;

import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BusCoreSystem entity.
 * 
 * @author OBUS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface BusCoreSystemRepository extends JpaRepository<BusCoreSystem, Long> {
    
    /**
     * Find bus core system by name (excluding soft deleted)
     * 
     * @param name the system name
     * @return Optional containing the system if found
     */
    Optional<BusCoreSystem> findByNameAndIsDeletedFalse(String name);
    
    /**
     * Find bus core system by UID (excluding soft deleted)
     * 
     * @param uid the system UID
     * @return Optional containing the system if found
     */
    Optional<BusCoreSystem> findByUidAndIsDeletedFalse(String uid);
    
    /**
     * Find systems by provider name (excluding soft deleted)
     * 
     * @param providerName the provider name
     * @return list of systems
     */
    List<BusCoreSystem> findByProviderNameAndIsDeletedFalse(String providerName);
    
    /**
     * Find the default system (excluding soft deleted)
     * 
     * @return Optional containing the default system if found
     */
    Optional<BusCoreSystem> findByIsDefaultTrueAndIsDeletedFalse();
    
    /**
     * Find all systems ordered by name (excluding soft deleted)
     * 
     * @return list of systems ordered by name
     */
    List<BusCoreSystem> findByIsDeletedFalseOrderByNameAsc();
    
    /**
     * Check if system name exists (excluding given id)
     * 
     * @param name the system name
     * @param id the system id to exclude
     * @return true if name exists, false otherwise
     */
    @Query("SELECT COUNT(s) > 0 FROM BusCoreSystem s WHERE s.name = :name AND s.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
    
    /**
     * Check if system name exists
     * 
     * @param name the system name
     * @return true if name exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find systems by base URL
     * 
     * @param baseUrl the base URL
     * @return list of systems with the given base URL
     */
    List<BusCoreSystem> findByBaseUrl(String baseUrl);
    
    /**
     * Count total number of systems
     * 
     * @return total count of systems
     */
    long count();
    
    /**
     * Find systems containing the given text in name or description
     * 
     * @param searchText the text to search for
     * @return list of matching systems
     */
    @Query("SELECT s FROM BusCoreSystem s WHERE s.name LIKE %:searchText% OR s.description LIKE %:searchText%")
    List<BusCoreSystem> findByNameContainingOrDescriptionContaining(@Param("searchText") String searchText);
}
