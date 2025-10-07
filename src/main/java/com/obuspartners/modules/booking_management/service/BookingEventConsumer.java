package com.obuspartners.modules.booking_management.service;

import com.obuspartners.modules.booking_management.domain.entity.Booking;
import com.obuspartners.modules.booking_management.domain.enums.BookingStatus;
import com.obuspartners.modules.booking_management.domain.event.BookingCreatedEvent;
import com.obuspartners.modules.booking_management.domain.event.BookingPaymentEvent;
import com.obuspartners.modules.booking_management.repository.BookingRepository;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.booking.BmsLgBookSeatService;
import com.obuspartners.modules.common.service.EventProducerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Service for consuming booking events from Kafka topics
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final EventProducerService eventProducerService;

    /**
     * Consume booking created events from the obus.booking.created topic
     */
    @KafkaListener(topics = "obus.booking.created", groupId = "obus-booking-consumer-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeBookingCreatedEvent(
            @Payload BookingCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received booking created event: {} from topic: {}, partition: {}, offset: {}",
                    event.getEventId(), topic, partition, offset);
            log.info("Booking UID: {}, Status: {}, Total Fare: {} {}",
                    event.getBookingUid(), event.getStatus(), event.getTotalBookingFare(), event.getCurrency());

            // Process the booking created event
            processBookingCreatedEvent(event);

            // Acknowledge the message
            acknowledgment.acknowledge();

            log.info("Successfully processed booking created event: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process booking created event: {}", event.getEventId(), e);
            // Don't acknowledge on error - message will be retried
            // In production, consider implementing dead letter queue for failed messages
        }
    }

    /**
     * Process booking created event
     */
    private void processBookingCreatedEvent(BookingCreatedEvent event) {
        log.info("Processing booking created event for booking UID: {}", event.getBookingUid());

        try {
            // Get the booking from database
            var booking = bookingService.getBookingByUid(event.getBookingUid());
            if (booking == null) {
                log.warn("Booking not found for UID: {}", event.getBookingUid());
                return;
            }

            // Process the booking based on business requirements
            processBookingBusinessLogic(booking, event);

            log.info("Booking created event processed successfully for UID: {}", event.getBookingUid());

        } catch (Exception e) {
            log.error("Error processing booking created event for UID: {}", event.getBookingUid(), e);
            throw e; // Re-throw to trigger retry logic
        }
    }

    /**
     * Process booking business logic after creation
     */
    private void processBookingBusinessLogic(com.obuspartners.modules.booking_management.domain.entity.Booking bk,
            BookingCreatedEvent event) {
        log.info("Processing business logic for booking UID: {}", bk.getUid());

        Booking booking = bookingService.getBookingByUid(bk.getUid());

        // Update booking status to PROCESSING
        booking.setStatus(BookingStatus.PROCESSING);
        booking = bookingRepository.save(booking);

        // Publish payment event to initiate payment processing
        publishBookingPaymentEvent(booking, event);

        log.info("Payment event published for booking UID: {}", booking.getUid());
    }

    /**
     * Publish booking payment event to initiate payment processing
     */
    private void publishBookingPaymentEvent(Booking booking, BookingCreatedEvent event) {
        try {
            BookingPaymentEvent paymentEvent = BookingPaymentEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .bookingUid(booking.getUid())
                    .bookingId(booking.getId())
                    .amount(booking.getTotalBookingFare())
                    .currency(booking.getCurrency())
                    .paymentMethod(booking.getPaymentMethod() != null ? booking.getPaymentMethod().name() : "CASH")
                    .paymentProvider(determinePaymentProvider(event.getPartnerCode()))
                    .partnerId(event.getPartnerId())
                    .partnerCode(event.getPartnerCode())
                    .agentId(event.getAgentId())
                    .agentCode(event.getAgentCode())
                    .customerPhone(booking.getPassengers().isEmpty() ? null : booking.getPassengers().get(0).getPhoneNumber())
                    .customerEmail(booking.getPassengers().isEmpty() ? null : booking.getPassengers().get(0).getEmail())
                    .customerName(booking.getPassengers().isEmpty() ? null : booking.getPassengers().get(0).getFullName())
                    .externalBookingId(booking.getExternalBookingId())
                    .externalReference(booking.getExternalReference())
                    .paymentProviderReference(generatePaymentProviderReference(booking))
                    .callbackUrl("/api/v1/payment/callback")
                    .returnUrl("/api/v1/payment/return")
                    .description("Bus booking payment for " + booking.getRouteName())
                    .notes("Booking created from " + booking.getBookingSource())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            eventProducerService.sendEvent("obus.booking.payment", booking.getUid(), paymentEvent);
            log.info("Booking payment event published for booking UID: {}", booking.getUid());

        } catch (Exception e) {
            log.error("Failed to publish booking payment event for booking UID: {}", booking.getUid(), e);
            throw e;
        }
    }

    /**
     * Determine payment provider based on partner code
     */
    private String determinePaymentProvider(String partnerCode) {
        if (partnerCode == null) {
            return "CASH";
        }
        
        switch (partnerCode.toUpperCase()) {
            case "MIXX":
                return "MIXX";
            case "BMSLG":
                return "BMSLG";
            default:
                return "CASH";
        }
    }

    /**
     * Generate payment provider reference
     */
    private String generatePaymentProviderReference(Booking booking) {
        return "PAY_" + booking.getUid() + "_" + System.currentTimeMillis();
    }

    // Placeholder methods for future implementation

    /**
     * Send booking confirmation notification
     */
    private void sendBookingConfirmationNotification(
            com.obuspartners.modules.booking_management.domain.entity.Booking booking) {
        log.info("Sending booking confirmation notification for UID: {}", booking.getUid());
        // TODO: Implement notification logic
    }

    /**
     * Update external booking systems
     */
    private void updateExternalBookingSystems(
            com.obuspartners.modules.booking_management.domain.entity.Booking booking) {
        log.info("Updating external booking systems for UID: {}", booking.getUid());
        // TODO: Implement external system updates
    }

    /**
     * Generate booking tickets
     */
    private void generateBookingTickets(com.obuspartners.modules.booking_management.domain.entity.Booking booking) {
        log.info("Generating tickets for booking UID: {}", booking.getUid());
        // TODO: Implement ticket generation
    }

    /**
     * Process booking payment
     */
    private void processBookingPayment(com.obuspartners.modules.booking_management.domain.entity.Booking booking) {
        log.info("Processing payment for booking UID: {}", booking.getUid());
        // TODO: Implement payment processing
    }

    /**
     * Update seat inventory
     */
    private void updateSeatInventory(com.obuspartners.modules.booking_management.domain.entity.Booking booking) {
        log.info("Updating seat inventory for booking UID: {}", booking.getUid());
        // TODO: Implement inventory updates
    }

    /**
     * Send booking analytics
     */
    private void sendBookingAnalytics(com.obuspartners.modules.booking_management.domain.entity.Booking booking) {
        log.info("Sending analytics for booking UID: {}", booking.getUid());
        // TODO: Implement analytics tracking
    }

    /**
     * Trigger partner-specific workflows
     */
    private void triggerPartnerWorkflows(com.obuspartners.modules.booking_management.domain.entity.Booking booking,
            BookingCreatedEvent event) {
        log.info("Triggering partner workflows for booking UID: {}", booking.getUid());

        // Example: Different workflows based on partner
        if (event.getPartnerCode() != null) {
            switch (event.getPartnerCode()) {
                case "MIXX":
                    processMixxPartnerWorkflow(booking, event);
                    break;
                case "BMSLG":
                    processBmslgPartnerWorkflow(booking, event);
                    break;
                default:
                    log.info("No specific workflow for partner: {}", event.getPartnerCode());
            }
        }
    }

    /**
     * Process MIXX partner specific workflow
     */
    private void processMixxPartnerWorkflow(com.obuspartners.modules.booking_management.domain.entity.Booking booking,
            BookingCreatedEvent event) {
        log.info("Processing MIXX partner workflow for booking UID: {}", booking.getUid());
        // TODO: Implement MIXX-specific logic
    }

    /**
     * Process BMSLG partner specific workflow
     */
    private void processBmslgPartnerWorkflow(com.obuspartners.modules.booking_management.domain.entity.Booking booking,
            BookingCreatedEvent event) {
        log.info("Processing BMSLG partner workflow for booking UID: {}", booking.getUid());
        // TODO: Implement BMSLG-specific logic
    }
}
