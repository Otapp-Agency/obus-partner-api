package com.obuspartners.modules.partner_integration.mixx.entity;

import com.obuspartners.modules.common.domain.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for storing MIXX payment callback data
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Entity
@Table(name = "mixx_payment_callbacks")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MixxPaymentCallback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;

    // Required fields from MIXX callback
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "mfs_transaction_id", nullable = false, length = 100)
    private String mfsTransactionId;

    @Column(name = "reference_id", nullable = false, length = 200)
    private String referenceId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 10)
    private String status; // "true" or "false"

    @Column(name = "callback_received_at", nullable = false)
    private LocalDateTime callbackReceivedAt;

    @Column(name = "callback_processed_at")
    private LocalDateTime callbackProcessedAt;

    @PostConstruct
    public void init() {
        if (this.uid == null) {
            this.uid = new de.huxhorn.sulky.ulid.ULID().nextULID();
        }
        if (this.callbackReceivedAt == null) {
            this.callbackReceivedAt = LocalDateTime.now();
        }
    }

    /**
     * Check if the payment was successful
     */
    public boolean isPaymentSuccessful() {
        return "true".equals(this.status);
    }

    /**
     * Mark callback as processed
     */
    public void markAsProcessed() {
        this.callbackProcessedAt = LocalDateTime.now();
    }
}
