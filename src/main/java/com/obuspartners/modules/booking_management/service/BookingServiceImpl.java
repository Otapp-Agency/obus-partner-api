package com.obuspartners.modules.booking_management.service;

import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.domain.entity.GroupAgentCoreBusSystem;
import com.obuspartners.modules.agent_management.repository.AgentRepository;
import com.obuspartners.modules.agent_management.repository.GroupAgentCoreBusSystemRepository;
import com.obuspartners.modules.booking_management.domain.dto.BookingResponseDto;
import com.obuspartners.modules.booking_management.domain.dto.CreateBookingRequestDto;
import com.obuspartners.modules.booking_management.domain.dto.PassengerDto;
import com.obuspartners.modules.booking_management.domain.entity.Booking;
import com.obuspartners.modules.booking_management.domain.entity.Passenger;
import com.obuspartners.modules.booking_management.domain.enums.BookingStatus;
import com.obuspartners.modules.booking_management.domain.enums.CancellationType;
import com.obuspartners.modules.booking_management.domain.enums.RefundStatus;
import com.obuspartners.modules.booking_management.domain.enums.TicketStatus;
import com.obuspartners.modules.booking_management.domain.event.BookingCreatedEvent;
import com.obuspartners.modules.booking_management.exception.BookingNotFoundException;
import com.obuspartners.modules.booking_management.exception.BookingValidationException;
import com.obuspartners.modules.booking_management.exception.PassengerNotFoundException;
import com.obuspartners.modules.booking_management.repository.BookingRepository;
import com.obuspartners.modules.booking_management.repository.PassengerRepository;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import com.obuspartners.modules.bus_core_system.repository.BusCoreSystemRepository;
import com.obuspartners.modules.common.exception.ApiException;
import com.obuspartners.modules.common.service.EventProducerService;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgBookSeatRequestDto;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.entity.BmsLgBooking;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.entity.BmsLgPassenger;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.repository.BmsLgBookingRepository;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.repository.BmsLgPassengerRepository;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for booking management operations
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final PartnerRepository partnerRepository;
    private final AgentRepository agentRepository;
    private final BusCoreSystemRepository busCoreSystemRepository;
    private final EventProducerService eventProducerService;
    private final BmsLgBookingRepository bmsLgBookingRepository;
    private final BmsLgPassengerRepository bmsLgPassengerRepository;
    private final GroupAgentCoreBusSystemRepository groupAgentCoreBusSystemRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(CreateBookingRequestDto request, BmsLgBookSeatRequestDto bmsLgRequest) {
        log.info("Creating booking for agent: {}, bus core system: {}",
                request.getAgentId(), request.getBusCoreSystemId());

        try {
            // 1. Validate booking request
            validateBookingRequest(request);

            // 2. Get related entities
            Partner partner;
            if (bmsLgRequest != null && bmsLgRequest.getPartnerId() != null) {
                // Use partner ID from BMSLG request if available
                partner = partnerRepository.findById(bmsLgRequest.getPartnerId())
                        .orElseThrow(() -> new BookingValidationException("Partner not found"));
            } else {
                // Fallback to getting partner from agent relationship
                partner = partnerRepository.findById(getPartnerIdFromAgent(request.getAgentId()))
                        .orElseThrow(() -> new BookingValidationException("Partner not found"));
            }

            Agent agent = agentRepository.findById(request.getAgentId())
                    .orElseThrow(() -> new BookingValidationException("Agent not found"));

            BusCoreSystem busCoreSystem = busCoreSystemRepository.findByCodeAndIsDeletedFalse("BMSLG")
                    .orElseThrow(() -> new ApiException("Bus System not found", HttpStatus.NOT_FOUND));

            GroupAgentCoreBusSystem groupAgentCoreBusSystem = groupAgentCoreBusSystemRepository
                    .findByGroupAgentAndBusCoreSystem(agent.getGroupAgent(), busCoreSystem)
                    .orElseThrow(() -> new ApiException("Group agent and system association not found",
                            HttpStatus.NOT_FOUND));

            // 3. Create booking entity
            Booking booking = createBookingEntity(request, partner, agent, busCoreSystem);

            // 4. Create passengers and calculate total fare
            List<Passenger> passengers = createPassengers(request.getPassengers(), booking);
            booking.setPassengers(passengers);

            // Calculate total fare
            BigDecimal totalFare = passengers.stream()
                    .map(Passenger::getIndividualFare)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            booking.setTotalBookingFare(totalFare);

            // 5. Save booking and passengers
            Booking savedBooking = bookingRepository.save(booking);
            log.info("Booking created successfully with UID: {}", savedBooking.getUid());

            // 6. Log BMSLG request data if provided (in separate transaction)
            if (bmsLgRequest != null) {
                if (groupAgentCoreBusSystem.getExternalAgentId() == null
                        || !groupAgentCoreBusSystem.getExternalAgentId().equals(bmsLgRequest.getAgentId())) {
                    throw new ApiException("External agent identifier does not match", HttpStatus.CONFLICT);
                }
                try {
                    logBmsLgRequestData(savedBooking, bmsLgRequest);
                } catch (Exception e) {
                    log.error(
                            "Failed to log BMSLG request data for booking UID: {}, but booking was created successfully",
                            savedBooking.getUid(), e);
                    // Don't rethrow - booking was already created successfully
                }
            }

            // 7. Publish booking created event
            publishBookingCreatedEvent(savedBooking, bmsLgRequest);

            // 8. Return response
            return new BookingResponseDto(savedBooking.getUid(), BookingStatus.PROCESSING,
                    "Booking received and is being processed");

        } catch (BookingValidationException e) {
            log.error("Booking validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during booking creation: {}", e.getMessage(), e);
            throw new BookingValidationException("Failed to create booking: " + e.getMessage());
        }
    }

    @Override
    public Booking getBookingByUid(String bookingUid) {
        return bookingRepository.findByUid(bookingUid)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with UID: " + bookingUid));
    }

    @Override
    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    @Override
    public Page<Booking> getBookingsByPartner(Long partnerId, Pageable pageable) {
        return bookingRepository.findByPartnerId(partnerId, pageable);
    }

    @Override
    public Page<Booking> getBookingsByAgent(Long agentId, Pageable pageable) {
        return bookingRepository.findByAgentId(agentId, pageable);
    }

    @Override
    public List<Passenger> getPassengersByBookingUid(String bookingUid) {
        return passengerRepository.findByBookingUid(bookingUid);
    }

    @Override
    public Passenger getPassengerByUid(String passengerUid) {
        return passengerRepository.findByUid(passengerUid)
                .orElseThrow(() -> new PassengerNotFoundException("Passenger not found with UID: " + passengerUid));
    }

    @Override
    @Transactional
    public Passenger cancelPassengerTicket(String passengerUid, String reason, String cancellationType) {
        Passenger passenger = getPassengerByUid(passengerUid);

        passenger.setIsCancelled(true);
        passenger.setCancelledAt(LocalDateTime.now());
        passenger.setCancellationReason(reason);
        passenger.setCancellationType(CancellationType.valueOf(cancellationType));
        passenger.setTicketStatus(TicketStatus.CANCELLED);

        return passengerRepository.save(passenger);
    }

    @Override
    @Transactional
    public Passenger processPassengerRefund(String passengerUid, BigDecimal refundAmount, String refundReference) {
        Passenger passenger = getPassengerByUid(passengerUid);

        passenger.setRefundAmount(refundAmount);
        passenger.setRefundReference(refundReference);
        passenger.setRefundStatus(RefundStatus.PROCESSED);
        passenger.setRefundProcessedAt(LocalDateTime.now());

        return passengerRepository.save(passenger);
    }

    /**
     * Validate booking request
     */
    private void validateBookingRequest(CreateBookingRequestDto request) {
        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            throw new BookingValidationException("At least one passenger is required");
        }

        if (request.getDepartureDate().isBefore(java.time.LocalDate.now())) {
            throw new BookingValidationException("Departure date cannot be in the past");
        }

        // Validate seat uniqueness
        // List<String> seatIds = request.getPassengers().stream()
        // .map(PassengerDto::getSeatId)
        // .toList();

        // if (seatIds.size() != seatIds.stream().distinct().count()) {
        // throw new BookingValidationException("Duplicate seat IDs found");
        // }

        // Check if seats are already occupied
        // for (String seatId : seatIds) {
        // if (passengerRepository.existsBySeatId(seatId)) {
        // throw new BookingValidationException("Seat " + seatId + " is already
        // occupied");
        // }
        // }
    }

    /**
     * Create booking entity from request
     */
    private Booking createBookingEntity(CreateBookingRequestDto request, Partner partner,
            Agent agent, BusCoreSystem busCoreSystem) {
        Booking booking = new Booking();

        // Relationships
        booking.setPartner(partner);
        booking.setAgent(agent);
        booking.setBusCoreSystem(busCoreSystem);

        // Company information
        booking.setCompanyName(request.getCompanyName());
        booking.setCompanyCode(request.getCompanyCode());
        booking.setCompanyRegistrationNumber(request.getCompanyRegistrationNumber());

        // Bus information
        booking.setBusNumber(request.getBusNumber());
        booking.setBusType(request.getBusType());
        booking.setBusModel(request.getBusModel());
        booking.setBusPlateNumber(request.getBusPlateNumber());
        booking.setBusCapacity(request.getBusCapacity());

        // Route information
        booking.setRouteName(request.getRouteName());
        booking.setDepartureStation(request.getDepartureStation());
        booking.setArrivalStation(request.getArrivalStation());
        booking.setDepartureDate(request.getDepartureDate());
        booking.setDepartureTime(request.getDepartureTime());
        booking.setArrivalTime(request.getArrivalTime());
        booking.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());

        // Fare information
        booking.setBaseFare(request.getBaseFare());
        booking.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        booking.setServiceCharge(request.getServiceCharge() != null ? request.getServiceCharge() : BigDecimal.ZERO);
        booking.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        booking.setCurrency(request.getCurrency());
        booking.setPaymentMethod(request.getPaymentMethod());

        // Metadata
        booking.setNotes(request.getNotes());
        booking.setBookingSource(request.getBookingSource());
        booking.setPromoCode(request.getPromoCode());

        return booking;
    }

    /**
     * Create passengers from request
     */
    private List<Passenger> createPassengers(List<PassengerDto> passengerDtos, Booking booking) {
        List<Passenger> passengers = new ArrayList<>();

        for (PassengerDto dto : passengerDtos) {
            Passenger passenger = new Passenger();

            // Personal details
            passenger.setFullName(dto.getFullName());
            passenger.setGender(dto.getGender());
            passenger.setCategory(dto.getCategory());
            passenger.setPassportNumber(dto.getPassportNumber());
            passenger.setNationalId(dto.getNationalId());
            passenger.setPhoneNumber(dto.getPhoneNumber());
            passenger.setEmail(dto.getEmail());

            // Route information
            passenger.setBoardingPoint(dto.getBoardingPoint());
            passenger.setDroppingPoint(dto.getDroppingPoint());
            passenger.setBoardingTime(dto.getBoardingTime());
            passenger.setDroppingTime(dto.getDroppingTime());

            // Seat and fare
            passenger.setSeatId(dto.getSeatId());
            passenger.setIndividualFare(dto.getIndividualFare());

            // Relationship
            passenger.setBooking(booking);

            passengers.add(passenger);
        }

        return passengers;
    }

    /**
     * Publish booking created event
     */
    private void publishBookingCreatedEvent(Booking booking, BmsLgBookSeatRequestDto bmsLgRequest) {
        try {
            BookingCreatedEvent event = BookingCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .bookingUid(booking.getUid())
                    .status(booking.getStatus().name())
                    .totalBookingFare(booking.getTotalBookingFare())
                    .currency(booking.getCurrency())
                    .paymentMethod(booking.getPaymentMethod() != null ? booking.getPaymentMethod().name() : null)
                    .paymentStatus(booking.getPaymentStatus().name())
                    .companyName(booking.getCompanyName())
                    .companyCode(booking.getCompanyCode())
                    .companyRegistrationNumber(booking.getCompanyRegistrationNumber())
                    .busNumber(booking.getBusNumber())
                    .busType(booking.getBusType())
                    .busModel(booking.getBusModel())
                    .busPlateNumber(booking.getBusPlateNumber())
                    .busCapacity(booking.getBusCapacity())
                    .routeName(booking.getRouteName())
                    .departureStation(booking.getDepartureStation())
                    .arrivalStation(booking.getArrivalStation())
                    .departureDate(booking.getDepartureDate())
                    .departureTime(booking.getDepartureTime())
                    .arrivalTime(booking.getArrivalTime())
                    .estimatedDurationMinutes(booking.getEstimatedDurationMinutes())
                    .partnerId(booking.getPartner() != null ? booking.getPartner().getId() : null)
                    .partnerUid(booking.getPartner() != null ? booking.getPartner().getUid() : null)
                    .partnerCode(booking.getPartner() != null ? booking.getPartner().getCode() : null)
                    .agentId(booking.getAgent() != null ? booking.getAgent().getId() : null)
                    .agentUid(booking.getAgent() != null ? booking.getAgent().getUid() : null)
                    .agentCode(booking.getAgent() != null ? booking.getAgent().getCode() : null)
                    .busCoreSystemId(booking.getBusCoreSystem() != null ? booking.getBusCoreSystem().getId() : null)
                    .busCoreSystemUid(booking.getBusCoreSystem() != null ? booking.getBusCoreSystem().getUid() : null)
                    .busCoreSystemCode(booking.getBusCoreSystem() != null ? booking.getBusCoreSystem().getCode() : null)
                    .externalBookingId(booking.getExternalBookingId())
                    .externalRouteId(booking.getExternalRouteId())
                    .externalBusId(booking.getExternalBusId())
                    .externalReference(booking.getExternalReference())
                    .notes(booking.getNotes())
                    .bookingSource(booking.getBookingSource())
                    .promoCode(booking.getPromoCode())
                    .timestamp(LocalDateTime.now())
                    .build();

            eventProducerService.sendEvent("obus.booking.created", booking.getUid(), event);
            log.info("Booking created event published for booking UID: {}", booking.getUid());

        } catch (Exception e) {
            log.error("Failed to publish booking created event for booking UID: {}", booking.getUid(), e);
            // Don't throw exception as booking is already saved
        }
    }

    /**
     * Get partner ID from agent (helper method)
     */
    private Long getPartnerIdFromAgent(Long agentId) {
        try {
            Agent agent = agentRepository.findById(agentId)
                    .orElseThrow(() -> new BookingValidationException("Agent not found"));

            if (agent.getGroupAgent() == null) {
                throw new BookingValidationException("Agent does not have a group agent");
            }

            if (agent.getGroupAgent().getPartner() == null) {
                throw new BookingValidationException("Group agent does not have a partner");
            }

            return agent.getGroupAgent().getPartner().getId();
        } catch (Exception e) {
            log.error("Error getting partner ID from agent {}: {}", agentId, e.getMessage());
            throw new BookingValidationException("Failed to get partner from agent: " + e.getMessage());
        }
    }

    /**
     * Log BMSLG request data in respective entities
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void logBmsLgRequestData(Booking booking, BmsLgBookSeatRequestDto bmsLgRequest) {
        try {
            log.info("Logging BMSLG request data for booking UID: {}", booking.getUid());

            // Create BmsLgBooking entity
            BmsLgBooking bmsLgBooking = new BmsLgBooking();

            // Initialize UID manually since @PostConstruct might not work in this context
            if (bmsLgBooking.getUid() == null) {
                bmsLgBooking.setUid(new de.huxhorn.sulky.ulid.ULID().nextULID());
            }

            bmsLgBooking.setImei(bmsLgRequest.getImei());
            bmsLgBooking.setLat(bmsLgRequest.getLat());
            bmsLgBooking.setLongitude(bmsLgRequest.getLongitude());
            bmsLgBooking.setIp(bmsLgRequest.getIp());
            bmsLgBooking.setOwnerId(bmsLgRequest.getOwnerId());
            bmsLgBooking.setAuthKey(bmsLgRequest.getAuthKey());
            bmsLgBooking.setAgentId(bmsLgRequest.getAgentId());

            // Log the values being set for debugging
            log.debug("Setting BMSLG booking values - ownerId: '{}', agentId: '{}', passengers: '{}'",
                    bmsLgBooking.getOwnerId(), bmsLgBooking.getAgentId(),
                    bmsLgBooking.getPassengers() != null
                            ? bmsLgBooking.getPassengers().substring(0,
                                    Math.min(100, bmsLgBooking.getPassengers().length())) + "..."
                            : "null");
            bmsLgBooking.setKey(bmsLgRequest.getKey());
            bmsLgBooking.setIsFrom(bmsLgRequest.getIsFrom());
            bmsLgBooking.setPltfm(bmsLgRequest.getPltfm());
            bmsLgBooking.setLang(bmsLgRequest.getLang());
            bmsLgBooking.setAppVer(bmsLgRequest.getAppVer());
            bmsLgBooking.setPassengers(bmsLgRequest.getPassengers());
            bmsLgBooking.setReturnPassengers(bmsLgRequest.getReturnPassengers());
            bmsLgBooking.setPhone(bmsLgRequest.getPhone());
            bmsLgBooking.setEmail(bmsLgRequest.getEmail());
            bmsLgBooking.setCurrency(bmsLgRequest.getCurrency());
            bmsLgBooking.setPayCode(bmsLgRequest.getPayCode());
            bmsLgBooking.setPayPhone(bmsLgRequest.getPayPhone());
            bmsLgBooking.setTranPass(bmsLgRequest.getTranPass());

            // Set relationship to main booking
            bmsLgBooking.setBooking(booking);

            // Validate required fields
            if (bmsLgBooking.getOwnerId() == null || bmsLgBooking.getOwnerId().trim().isEmpty()) {
                throw new IllegalArgumentException("Owner ID is required for BMSLG booking");
            }
            if (bmsLgBooking.getAgentId() == null || bmsLgBooking.getAgentId().trim().isEmpty()) {
                throw new IllegalArgumentException("Agent ID is required for BMSLG booking");
            }
            if (bmsLgBooking.getPassengers() == null || bmsLgBooking.getPassengers().trim().isEmpty()) {
                throw new IllegalArgumentException("Passengers data is required for BMSLG booking");
            }

            // Save BmsLgBooking
            log.debug("Attempting to save BmsLgBooking with ownerId: '{}', agentId: '{}'",
                    bmsLgBooking.getOwnerId(), bmsLgBooking.getAgentId());
            BmsLgBooking savedBmsLgBooking = bmsLgBookingRepository.save(bmsLgBooking);
            log.info("BmsLgBooking saved with UID: {}", savedBmsLgBooking.getUid());

            // Parse passengers JSON and create BmsLgPassenger entities
            if (bmsLgRequest.getPassengers() != null && !bmsLgRequest.getPassengers().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> passengersMainData = objectMapper
                            .readValue(bmsLgRequest.getPassengers(), java.util.Map.class);

                    @SuppressWarnings("unchecked")
                    java.util.List<java.util.Map<String, Object>> passengersData = (java.util.List<java.util.Map<String, Object>>) passengersMainData
                            .get("passengers");

                    if (passengersData != null) {
                        for (java.util.Map<String, Object> passengerData : passengersData) {
                            BmsLgPassenger bmsLgPassenger = new BmsLgPassenger();

                            // Initialize UID manually
                            if (bmsLgPassenger.getUid() == null) {
                                bmsLgPassenger.setUid(new de.huxhorn.sulky.ulid.ULID().nextULID());
                            }

                            // Passenger basic info
                            bmsLgPassenger.setName((String) passengerData.get("name"));
                            bmsLgPassenger.setGender((String) passengerData.get("gender"));
                            bmsLgPassenger.setCategory((String) passengerData.get("category"));
                            bmsLgPassenger.setPassport((String) passengerData.get("passport"));
                            bmsLgPassenger.setSeatId((String) passengerData.get("seat_id"));
                            bmsLgPassenger.setSeatTypeId((String) passengerData.get("seat_type_id"));
                            bmsLgPassenger.setSeatMob((String) passengerData.get("seat_mob"));
                            bmsLgPassenger.setEmail((String) passengerData.get("email"));
                            bmsLgPassenger.setNewSeatFare((String) passengerData.get("new_seat_fare"));

                            // Route info from main passengers data
                            bmsLgPassenger.setApbiId((String) passengersMainData.get("apbi_id"));
                            bmsLgPassenger.setFromId((String) passengersMainData.get("from_id"));
                            bmsLgPassenger.setToId((String) passengersMainData.get("to_id"));
                            bmsLgPassenger.setTrvlDt((String) passengersMainData.get("trvl_dt"));
                            bmsLgPassenger.setSubId((String) passengersMainData.get("sub_id"));
                            bmsLgPassenger.setTdiId((String) passengersMainData.get("tdi_id"));
                            bmsLgPassenger.setLbId((String) passengersMainData.get("lb_id"));
                            bmsLgPassenger.setPbiId((String) passengersMainData.get("pbi_id"));
                            bmsLgPassenger.setAsiId((String) passengersMainData.get("asi_id"));
                            bmsLgPassenger.setUkey((String) passengersMainData.get("ukey"));
                            bmsLgPassenger.setBoarding((String) passengersMainData.get("boarding"));
                            bmsLgPassenger.setDropping((String) passengersMainData.get("dropping"));
                            bmsLgPassenger.setBoardingTime((String) passengersMainData.get("boarding_time"));
                            bmsLgPassenger.setDroppingTime((String) passengersMainData.get("dropping_time"));

                            // Validate required fields for passenger
                            if (bmsLgPassenger.getName() == null || bmsLgPassenger.getName().trim().isEmpty()) {
                                log.warn("Passenger name is empty, skipping passenger");
                                continue;
                            }
                            if (bmsLgPassenger.getSeatId() == null || bmsLgPassenger.getSeatId().trim().isEmpty()) {
                                log.warn("Passenger seat ID is empty, skipping passenger");
                                continue;
                            }

                            // Set relationship
                            bmsLgPassenger.setBmsLgBooking(savedBmsLgBooking);

                            // Save BmsLgPassenger
                            log.debug("Attempting to save BmsLgPassenger with name: '{}', seatId: '{}'",
                                    bmsLgPassenger.getName(), bmsLgPassenger.getSeatId());
                            bmsLgPassengerRepository.save(bmsLgPassenger);
                            log.debug("BmsLgPassenger saved successfully");
                        }
                        log.info("Saved {} BmsLgPassenger entities for booking UID: {}", passengersData.size(),
                                booking.getUid());
                    }
                } catch (Exception e) {
                    log.error("Failed to parse passengers JSON for BMSLG logging: {}", e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("Failed to log BMSLG request data for booking UID: {}", booking.getUid(), e);
            // Don't throw exception as main booking is already saved
        }
    }
}