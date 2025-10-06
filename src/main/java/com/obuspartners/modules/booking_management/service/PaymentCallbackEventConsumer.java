package com.obuspartners.modules.booking_management.service;

import com.obuspartners.modules.booking_management.domain.entity.Booking;
import com.obuspartners.modules.booking_management.domain.enums.BookingStatus;
import com.obuspartners.modules.booking_management.domain.event.PaymentCallbackEvent;
import com.obuspartners.modules.booking_management.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Service for consuming payment callback events from Kafka topics
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackEventConsumer {

    private final BookingRepository bookingRepository;

    /**
     * Consume payment callback events from the obus.payment.callback topic
     */
    @KafkaListener(
        topics = "obus.payment.callback",
        groupId = "obus-payment-callback-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentCallbackEvent(
            @Payload PaymentCallbackEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received payment callback event: {} from topic: {}, partition: {}, offset: {}", 
                    event.getEventId(), topic, partition, offset);
            log.info("Booking UID: {}, Payment Status: {}, Provider: {}", 
                    event.getBookingUid(), event.getPaymentStatus(), event.getPaymentProvider());
            
            // Process the payment callback event
            processPaymentCallbackEvent(event);
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            
            log.info("Successfully processed payment callback event: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process payment callback event: {}", event.getEventId(), e);
            // Don't acknowledge on error - message will be retried
        }
    }

    /**
     * Process payment callback event
     */
    private void processPaymentCallbackEvent(PaymentCallbackEvent event) {
        log.info("Processing payment callback for booking UID: {}", event.getBookingUid());
        
        try {
            // Get the booking from database
            Booking booking = bookingRepository.findByUid(event.getBookingUid())
                    .orElseThrow(() -> new RuntimeException("Booking not found for UID: " + event.getBookingUid()));
            
            // Process the callback based on payment status
            processPaymentCallbackByStatus(booking, event);
            
            log.info("Payment callback processed successfully for booking UID: {}", event.getBookingUid());
            
        } catch (Exception e) {
            log.error("Error processing payment callback for booking UID: {}", event.getBookingUid(), e);
            throw e; // Re-throw to trigger retry logic
        }
    }

    /**
     * Process payment callback based on status
     */
    private void processPaymentCallbackByStatus(Booking booking, PaymentCallbackEvent event) {
        log.info("Processing payment callback with status: {} for booking UID: {}", 
                event.getPaymentStatus(), booking.getUid());
        
        switch (event.getPaymentStatus().toUpperCase()) {
            case "SUCCESS":
                processSuccessfulPayment(booking, event);
                break;
            case "FAILED":
                processFailedPayment(booking, event);
                break;
            case "PENDING":
                processPendingPayment(booking, event);
                break;
            case "CANCELLED":
                processCancelledPayment(booking, event);
                break;
            default:
                log.warn("Unknown payment status: {} for booking UID: {}", 
                        event.getPaymentStatus(), booking.getUid());
                processFailedPayment(booking, event);
        }
    }

    /**
     * Process successful payment
     */
    private void processSuccessfulPayment(Booking booking, PaymentCallbackEvent event) {
        log.info("Processing successful payment for booking UID: {}", booking.getUid());
        
        try {
            // Update booking status to CONFIRMED
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPaymentStatus(com.obuspartners.modules.booking_management.domain.enums.PaymentStatus.PAID);
            
            // Store payment details
            booking.setExternalBookingId(event.getTransactionId());
            booking.setExternalReference(event.getPaymentProviderReference());
            
            // Save booking
            bookingRepository.save(booking);
            
            // Trigger post-payment success workflows
            triggerPostPaymentSuccessWorkflows(booking, event);
            
            log.info("Successful payment processed for booking UID: {}", booking.getUid());
            
        } catch (Exception e) {
            log.error("Error processing successful payment for booking UID: {}", booking.getUid(), e);
            throw e;
        }
    }

    /**
     * Process failed payment
     */
    private void processFailedPayment(Booking booking, PaymentCallbackEvent event) {
        log.info("Processing failed payment for booking UID: {}", booking.getUid());
        
        try {
            // Update booking status to FAILED
            booking.setStatus(BookingStatus.FAILED);
            booking.setPaymentStatus(com.obuspartners.modules.booking_management.domain.enums.PaymentStatus.FAILED);
            
            // Store failure reason
            booking.setNotes(booking.getNotes() + " | Payment failed: " + event.getFailureReason());
            
            // Save booking
            bookingRepository.save(booking);
            
            // Trigger post-payment failure workflows
            triggerPostPaymentFailureWorkflows(booking, event);
            
            log.info("Failed payment processed for booking UID: {}", booking.getUid());
            
        } catch (Exception e) {
            log.error("Error processing failed payment for booking UID: {}", booking.getUid(), e);
            throw e;
        }
    }

    /**
     * Process pending payment
     */
    private void processPendingPayment(Booking booking, PaymentCallbackEvent event) {
        log.info("Processing pending payment for booking UID: {}", booking.getUid());
        
        try {
            // Keep booking status as PROCESSING
            booking.setPaymentStatus(com.obuspartners.modules.booking_management.domain.enums.PaymentStatus.PENDING);
            
            // Save booking
            bookingRepository.save(booking);
            
            log.info("Pending payment processed for booking UID: {}", booking.getUid());
            
        } catch (Exception e) {
            log.error("Error processing pending payment for booking UID: {}", booking.getUid(), e);
            throw e;
        }
    }

    /**
     * Process cancelled payment
     */
    private void processCancelledPayment(Booking booking, PaymentCallbackEvent event) {
        log.info("Processing cancelled payment for booking UID: {}", booking.getUid());
        
        try {
            // Update booking status to CANCELLED
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(com.obuspartners.modules.booking_management.domain.enums.PaymentStatus.FAILED);
            
            // Save booking
            bookingRepository.save(booking);
            
            log.info("Cancelled payment processed for booking UID: {}", booking.getUid());
            
        } catch (Exception e) {
            log.error("Error processing cancelled payment for booking UID: {}", booking.getUid(), e);
            throw e;
        }
    }

    /**
     * Trigger post-payment success workflows
     */
    private void triggerPostPaymentSuccessWorkflows(Booking booking, PaymentCallbackEvent event) {
        log.info("Triggering post-payment success workflows for booking UID: {}", booking.getUid());
        
        // Example workflows:
        // 1. Send confirmation email/SMS
        // 2. Generate tickets
        // 3. Update seat inventory
        // 4. Send analytics events
        // 5. Trigger partner-specific workflows
        
        // Partner-specific workflows
        if (event.getPartnerCode() != null) {
            switch (event.getPartnerCode().toUpperCase()) {
                case "MIXX":
                    processMixxPostPaymentSuccess(booking, event);
                    break;
                case "BMSLG":
                    processBmslgPostPaymentSuccess(booking, event);
                    break;
                default:
                    log.info("No specific post-payment workflow for partner: {}", event.getPartnerCode());
            }
        }
    }

    /**
     * Trigger post-payment failure workflows
     */
    private void triggerPostPaymentFailureWorkflows(Booking booking, PaymentCallbackEvent event) {
        log.info("Triggering post-payment failure workflows for booking UID: {}", booking.getUid());
        
        // Example workflows:
        // 1. Send failure notification
        // 2. Release seat inventory
        // 3. Send analytics events
        // 4. Trigger retry mechanisms
    }

    /**
     * Process MIXX post-payment success workflow
     */
    private void processMixxPostPaymentSuccess(Booking booking, PaymentCallbackEvent event) {
        log.info("Processing MIXX post-payment success workflow for booking UID: {}", booking.getUid());
        // TODO: Implement MIXX-specific post-payment logic
    }

    /**
     * Process BMSLG post-payment success workflow
     */
    private void processBmslgPostPaymentSuccess(Booking booking, PaymentCallbackEvent event) {
        log.info("Processing BMSLG post-payment success workflow for booking UID: {}", booking.getUid());
        // TODO: Implement BMSLG-specific post-payment logic
    }
}
