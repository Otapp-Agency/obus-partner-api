package com.obuspartners.modules.partner_integration.mixx.repository;

import com.obuspartners.modules.partner_integration.mixx.entity.MixxPaymentCallback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for MixxPaymentCallback entity operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Repository
public interface MixxPaymentCallbackRepository extends JpaRepository<MixxPaymentCallback, Long> {

    /**
     * Find callback by UID
     */
    Optional<MixxPaymentCallback> findByUid(String uid);

    /**
     * Find callback by MFS Transaction ID
     */
    Optional<MixxPaymentCallback> findByMfsTransactionId(String mfsTransactionId);

    /**
     * Find callback by Reference ID
     */
    Optional<MixxPaymentCallback> findByReferenceId(String referenceId);

    /**
     * Check if callback exists by MFS Transaction ID
     */
    boolean existsByMfsTransactionId(String mfsTransactionId);

    /**
     * Check if callback exists by Reference ID
     */
    boolean existsByReferenceId(String referenceId);
}
