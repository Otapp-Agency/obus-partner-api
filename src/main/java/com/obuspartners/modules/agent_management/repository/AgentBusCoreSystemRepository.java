package com.obuspartners.modules.agent_management.repository;

import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.domain.entity.AgentBusCoreSystem;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AgentBusCoreSystem entity
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface AgentBusCoreSystemRepository extends JpaRepository<AgentBusCoreSystem, Long> {

    /**
     * Find AgentBusCoreSystem by agent and bus core system
     */
    Optional<AgentBusCoreSystem> findByAgentAndBusCoreSystem(Agent agent, BusCoreSystem busCoreSystem);

    /**
     * Find AgentBusCoreSystem by UID
     */
    Optional<AgentBusCoreSystem> findByUid(String uid);

    /**
     * Find all AgentBusCoreSystem by agent
     */
    List<AgentBusCoreSystem> findByAgent(Agent agent);

    /**
     * Find all AgentBusCoreSystem by bus core system
     */
    List<AgentBusCoreSystem> findByBusCoreSystem(BusCoreSystem busCoreSystem);

    /**
     * Find active AgentBusCoreSystem by agent
     */
    List<AgentBusCoreSystem> findByAgentAndIsActiveTrue(Agent agent);

    /**
     * Find primary AgentBusCoreSystem for an agent
     */
    Optional<AgentBusCoreSystem> findByAgentAndIsPrimaryTrue(Agent agent);

    /**
     * Find AgentBusCoreSystem by agent login name and bus core system
     */
    Optional<AgentBusCoreSystem> findByAgentLoginNameAndBusCoreSystem(String agentLoginName, BusCoreSystem busCoreSystem);

    /**
     * Check if agent login name exists for a specific bus core system
     */
    boolean existsByAgentLoginNameAndBusCoreSystem(String agentLoginName, BusCoreSystem busCoreSystem);

    /**
     * Find all agents assigned to a bus core system
     */
    @Query("SELECT abcs.agent FROM AgentBusCoreSystem abcs WHERE abcs.busCoreSystem = :busCoreSystem AND abcs.isActive = true")
    List<Agent> findActiveAgentsByBusCoreSystem(@Param("busCoreSystem") BusCoreSystem busCoreSystem);

    /**
     * Find all bus core systems assigned to an agent
     */
    @Query("SELECT abcs.busCoreSystem FROM AgentBusCoreSystem abcs WHERE abcs.agent = :agent AND abcs.isActive = true")
    List<BusCoreSystem> findActiveBusCoreSystemsByAgent(@Param("agent") Agent agent);

    /**
     * Find all bus core systems assigned to an agent by agent ID
     */
    @Query("SELECT abcs FROM AgentBusCoreSystem abcs WHERE abcs.agent.id = :agentId")
    List<AgentBusCoreSystem> findByAgentId(@Param("agentId") Long agentId);

    /**
     * Find all bus core systems assigned to an agent by agent UID
     */
    @Query("SELECT abcs FROM AgentBusCoreSystem abcs WHERE abcs.agent.uid = :agentUid")
    List<AgentBusCoreSystem> findByAgentUid(@Param("agentUid") String agentUid);

    /**
     * Find all agents assigned to a bus core system by bus core system ID
     */
    @Query("SELECT abcs FROM AgentBusCoreSystem abcs WHERE abcs.busCoreSystem.id = :busCoreSystemId")
    List<AgentBusCoreSystem> findByBusCoreSystemId(@Param("busCoreSystemId") Long busCoreSystemId);

    /**
     * Count active agents for a bus core system
     */
    long countByBusCoreSystemAndIsActiveTrue(BusCoreSystem busCoreSystem);

    /**
     * Count active bus core systems for an agent
     */
    long countByAgentAndIsActiveTrue(Agent agent);
}
