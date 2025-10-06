package com.obuspartners.modules.partner_integration.bmslg.agent_v8.booking;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obuspartners.modules.agent_management.domain.entity.Agent;
import com.obuspartners.modules.agent_management.repository.AgentRepository;
import com.obuspartners.modules.booking_management.domain.dto.BookingResponseDto;
import com.obuspartners.modules.booking_management.domain.dto.CreateBookingRequestDto;
import com.obuspartners.modules.booking_management.domain.dto.PassengerDto;
import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgBookSeatRequestDto;
import com.obuspartners.modules.partner_management.domain.entity.Partner;
import com.obuspartners.modules.partner_management.repository.PartnerRepository;
import com.obuspartners.modules.booking_management.domain.entity.Booking;
import com.obuspartners.modules.booking_management.domain.entity.Passenger;
import com.obuspartners.modules.booking_management.domain.enums.BookingStatus;
import com.obuspartners.modules.booking_management.domain.enums.Gender;
import com.obuspartners.modules.booking_management.domain.enums.PassengerCategory;
import com.obuspartners.modules.booking_management.domain.enums.PaymentMethod;
import com.obuspartners.modules.booking_management.domain.enums.TicketStatus;
import com.obuspartners.modules.booking_management.service.BookingService;
import com.obuspartners.modules.bus_core_system.domain.entity.BusCoreSystem;
import com.obuspartners.modules.bus_core_system.repository.BusCoreSystemRepository;
import com.obuspartners.modules.common.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for booking seat data from BMSLG API.
 * 
 * This service implements the Book-Seat functionality as specified in the PHP script,
 * including proper authentication key generation, passenger data handling, and transaction password encryption.
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BmsLgBookSeatServiceImpl implements BmsLgBookSeatService {

    private static final String BMSLG_BOOK_SEAT_URL = "https://bms.oacl.co.tz/api/Agents-V8/Book-Seat.php";
    private static final String BOOK_SALT = "b0Ok";
    private static final String BOOK_POST_SALT = "B0Ok5e@t";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BookingService bookingService;
    private final AgentRepository agentRepository;
    private final PartnerRepository partnerRepository;
    private final BusCoreSystemRepository busCoreSystemRepository;

    @Override
    public BookingResponseDto createBookingRequest(BmsLgBookSeatRequestDto bookingRequest) {
        log.info("Create booking request from BMSLG");
        try {
            // Extract data from DTO
            // Partner  
            Long partnerAgentId = bookingRequest.getPartnerAgentId();
            Long partnerId = bookingRequest.getPartnerId();

            Agent agent = agentRepository.findById(partnerAgentId).orElseThrow(() -> new ApiException("Agent not found", HttpStatus.NOT_FOUND));
            Partner partner = partnerRepository.findById(partnerId).orElseThrow(() -> new ApiException("Partner not found", HttpStatus.NOT_FOUND));
            BusCoreSystem busCoreSystem = busCoreSystemRepository.findByCodeAndIsDeletedFalse("BMSLG").orElseThrow(() -> new ApiException("Bus core system not found", HttpStatus.NOT_FOUND));

            // BMSLG
            String ownerId = bookingRequest.getOwnerId();
            String currency = bookingRequest.getCurrency();
            String payCode = bookingRequest.getPayCode();
            String passengersJson = bookingRequest.getPassengers();

            // Parse passengers JSON to extract data
            @SuppressWarnings("unchecked")
            Map<String, Object> passengersMainData = objectMapper.readValue(passengersJson, Map.class);

            Booking booking = new Booking();
            // List<Passenger> passengers = new ArrayList<Passenger>();
            
            // Direct population of booking with BMSLG data
            booking.setPartner(partner);
            booking.setAgent(agent);
            booking.setBusCoreSystem(busCoreSystem);
            booking.setCompanyName("No name");
            booking.setCompanyCode(null);
            booking.setCompanyRegistrationNumber(null);
            booking.setBusNumber(null);
            booking.setBusType(null);
            booking.setBusModel(null);
            booking.setBusPlateNumber(null);
            booking.setBusCapacity(null);
            booking.setRouteName(null);
            booking.setDepartureStation(null);
            booking.setArrivalStation(null);
            booking.setDepartureDate(null);
            booking.setDepartureTime(null);
            booking.setArrivalTime(null);
            booking.setEstimatedDurationMinutes(null);
            booking.setTotalBookingFare(null);
            booking.setBaseFare(null);
            booking.setTaxAmount(null);
            booking.setServiceCharge(null);
            booking.setDiscountAmount(null);
            booking.setCurrency(currency);
            booking.setPaymentMethod(PaymentMethod.valueOf(payCode));
            booking.setPaymentStatus(null);
            booking.setStatus(BookingStatus.PROCESSING);
            booking.setExternalBookingId(null);
            booking.setExternalRouteId(null);
            booking.setExternalBusId(null);
            booking.setExternalReference(ownerId);
            booking.setNotes("Created from BMSLG integration");
            booking.setBookingSource("BMSLG_API");
            booking.setPromoCode(null);
            // booking.setPassengers(passengers);
            
            // Populate passengers from BMSLG data
            booking = populatePassengersFromBmslgData(booking, passengersMainData);
            
            CreateBookingRequestDto request = new CreateBookingRequestDto();

            // === BASIC BOOKING INFO ===
            request.setAgentId(partnerAgentId);
            request.setBusCoreSystemId(busCoreSystem.getId());

            // === COMPANY INFORMATION ===
            request.setCompanyName("Test Company"); // Use partner name as company name
            request.setCompanyCode(partner.getCode());
            // request.setCompanyRegistrationNumber(partner.getRegistrationNumber());

            // === BUS INFORMATION ===
            request.setBusNumber("BMSLG-" + ownerId); // Use ownerId as bus identifier
            request.setBusType("EXPRESS");
            request.setBusModel("BMSLG Bus");
            request.setBusPlateNumber(null);
            request.setBusCapacity(50); // Default capacity

            // === ROUTE INFORMATION ===
            request.setRouteName("BMSLG Route");
            request.setDepartureStation("BMSLG Departure");
            request.setArrivalStation("BMSLG Arrival");
            
            // Parse date from passengers data
            String travelDateStr = (String) passengersMainData.get("trvl_dt");
            if (travelDateStr != null) {
                request.setDepartureDate(LocalDate.parse(travelDateStr));
            } else {
                request.setDepartureDate(LocalDate.now());
            }
            
            // Parse times from passengers data
            String boardingTimeStr = (String) passengersMainData.get("boarding_time");
            String droppingTimeStr = (String) passengersMainData.get("dropping_time");
            
            if (boardingTimeStr != null) {
                request.setDepartureTime(LocalTime.parse(boardingTimeStr));
            } else {
                request.setDepartureTime(LocalTime.of(8, 0)); // Default 8:00 AM
            }
            
            if (droppingTimeStr != null) {
                request.setArrivalTime(LocalTime.parse(droppingTimeStr));
            } else {
                request.setArrivalTime(LocalTime.of(15, 0)); // Default 3:00 PM
            }
            
            request.setEstimatedDurationMinutes(420); // 7 hours default

            // === FARE INFORMATION ===
            request.setBaseFare(BigDecimal.ZERO); // Will be calculated from passengers
            request.setTaxAmount(BigDecimal.ZERO);
            request.setServiceCharge(BigDecimal.ZERO);
            request.setDiscountAmount(BigDecimal.ZERO);
            request.setCurrency("TZS"); // Default currency
            request.setPaymentMethod(PaymentMethod.valueOf(payCode != null ? payCode : "CASH"));

            // === PASSENGERS ===
            List<PassengerDto> passengerDtos = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> passengersData = (List<Map<String, Object>>) passengersMainData.get("passengers");
            
            BigDecimal totalFare = BigDecimal.ZERO;
            
            if (passengersData != null) {
                for (Map<String, Object> passengerData : passengersData) {
                    PassengerDto passengerDto = new PassengerDto();
                    
                    // Basic passenger info
                    passengerDto.setFullName((String) passengerData.get("name"));
                    
                    // Gender mapping
                    String genderStr = (String) passengerData.get("gender");
                    if ("Male".equalsIgnoreCase(genderStr)) {
                        passengerDto.setGender(Gender.MALE);
                    } else if ("Female".equalsIgnoreCase(genderStr)) {
                        passengerDto.setGender(Gender.FEMALE);
                    } else {
                        passengerDto.setGender(Gender.MALE); // Default
                    }
                    
                    // Category mapping
                    String categoryStr = (String) passengerData.get("category");
                    if ("Adult".equalsIgnoreCase(categoryStr)) {
                        passengerDto.setCategory(PassengerCategory.ADULT);
                    } else if ("Child".equalsIgnoreCase(categoryStr)) {
                        passengerDto.setCategory(PassengerCategory.CHILD);
                    } else if ("Infant".equalsIgnoreCase(categoryStr)) {
                        passengerDto.setCategory(PassengerCategory.INFANT);
                    } else {
                        passengerDto.setCategory(PassengerCategory.ADULT); // Default
                    }
                    
                    // Route info
                    passengerDto.setBoardingPoint((String) passengersMainData.get("boarding"));
                    passengerDto.setDroppingPoint((String) passengersMainData.get("dropping"));
                    
                    if (boardingTimeStr != null) {
                        passengerDto.setBoardingTime(LocalTime.parse(boardingTimeStr));
                    }
                    if (droppingTimeStr != null) {
                        passengerDto.setDroppingTime(LocalTime.parse(droppingTimeStr));
                    }
                    
                    // Seat and fare
                    passengerDto.setSeatId((String) passengerData.get("seat_id"));
                    
                    String fareStr = (String) passengerData.get("new_seat_fare");
                    BigDecimal individualFare = BigDecimal.ZERO;
                    if (fareStr != null && !fareStr.isEmpty()) {
                        try {
                            individualFare = new BigDecimal(fareStr);
                        } catch (NumberFormatException e) {
                            individualFare = BigDecimal.valueOf(25000); // Default fare
                        }
                    } else {
                        individualFare = BigDecimal.valueOf(25000); // Default fare
                    }
                    passengerDto.setIndividualFare(individualFare);
                    totalFare = totalFare.add(individualFare);
                    
                    // Contact info
                    passengerDto.setPhoneNumber((String) passengerData.get("seat_mob"));
                    passengerDto.setEmail((String) passengerData.get("email"));
                    passengerDto.setPassportNumber((String) passengerData.get("passport"));
                    
                    passengerDtos.add(passengerDto);
                }
            }
            
            request.setPassengers(passengerDtos);
            request.setBaseFare(totalFare); // Set total fare as base fare

            // === METADATA ===
            request.setNotes("Created from BMSLG integration");
            request.setBookingSource("BMSLG_API");
            request.setPromoCode(null);

            return bookingService.createBooking(request, bookingRequest);
        } catch (Exception e) {
            log.error("Error during BMSLG seat booking", e);
            //throw new ApiException("Failed to process Booking: " + ExceptionUtils.getStackTrace(e), HttpStatus.INTERNAL_SERVER_ERROR);
            throw new ApiException("Failed to process Booking: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Object bookSeat(BmsLgBookSeatRequestDto bookingRequest) {
        log.info("Booking seat data from BMSLG");
        try {
            // Generate random auth key (1-4, excluding RIPEMD160)
            Random random = new Random();
            int authKey = random.nextInt(4) + 1; // Random between 1-4
            String authKeyStr = String.valueOf(authKey);

            // Define parameters as per PHP script
            String ownerId = "1";
            String agentId = "101";
            String rawTranPass = "agentTransactionPassword";
            String imei = "123456789012345";
            String lat = "0.0000";
            String longitude = "0.0000";
            String ip = "127.0.0.1";
            String appVer = "1.0.0";
            String isFrom = "0";
            String currency = "1";
            String phone = "255700000000";
            String email = "test@example.com";
            String payCode = "CASH";
            String pltfm = "0";
            String lang = "0";

            // Create passenger data structure
            Map<String, Object> passengersMainData = createPassengersData();
            String passengersJsonString = objectMapper.writeValueAsString(passengersMainData);
            String returnPassengersJsonString = ""; // Empty for one-way

            // Encrypt the raw transaction password
            String tranPassForPost = encrypt(authKey, rawTranPass);

            // Prepare POST fields
            Map<String, String> postFields = new HashMap<>();
            postFields.put("owner_id", ownerId);
            postFields.put("agent_id", agentId);
            postFields.put("auth_key", authKeyStr);
            postFields.put("imei", imei);
            postFields.put("lat", lat);
            postFields.put("long", longitude);
            postFields.put("ip", ip);
            postFields.put("app_ver", appVer);
            postFields.put("is_from", isFrom);
            postFields.put("currency", currency);
            postFields.put("phone", phone);
            postFields.put("email", email);
            postFields.put("passengers", passengersJsonString);
            postFields.put("return_passengers", returnPassengersJsonString);
            postFields.put("pay_code", payCode);
            postFields.put("tran_pass", tranPassForPost);
            postFields.put("pltfm", pltfm);
            postFields.put("lang", lang);

            // Generate GET key: encrypt(auth_key, md5(sha512(agent_id + owner_id + auth_key + "b0Ok")))
            String getKeyInput = agentId + ownerId + authKeyStr + BOOK_SALT;
            String getKeyString = md5(sha512(getKeyInput));
            String getKeyValue = encrypt(authKey, getKeyString);

            // Generate POST key: encrypt(auth_key, md5(sha512(owner_id + agent_id + tran_pass + passengers + return_passengers + phone + email + currency + pay_code + is_from + pltfm + lang + imei + lat + long + ip + app_ver + auth_key + "B0Ok5e@t")))
            String postKeyInput = postFields.get("owner_id") + postFields.get("agent_id") + 
                                postFields.get("tran_pass") + postFields.get("passengers") + 
                                postFields.get("return_passengers") + postFields.get("phone") + 
                                postFields.get("email") + postFields.get("currency") + 
                                postFields.get("pay_code") + postFields.get("is_from") + 
                                postFields.get("pltfm") + postFields.get("lang") + 
                                postFields.get("imei") + postFields.get("lat") + 
                                postFields.get("long") + postFields.get("ip") + 
                                postFields.get("app_ver") + postFields.get("auth_key") + 
                                BOOK_POST_SALT;
            String postKeyString = md5(sha512(postKeyInput));
            String postKeyValue = encrypt(authKey, postKeyString);
            postFields.put("key", postKeyValue);

            // Build URL with GET parameters
            String url = BMSLG_BOOK_SEAT_URL + "?key=" + getKeyValue;

            // Build POST body
            StringBuilder postBody = new StringBuilder();
            postFields.forEach((key, value) ->
                postBody.append(key).append("=").append(value).append("&"));
            String postBodyStr = postBody.toString().replaceAll("&$", "");

            log.info("BMSLG Book Seat URL: {}", url);
            log.info("BMSLG Book Seat Payload: {}", postBodyStr);

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postBodyStr))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("BMSLG Book Seat HTTP Code: {}", response.statusCode());
            log.info("BMSLG Book Seat Response: {}", response.body());

            return response.body();

        } catch (IOException | InterruptedException e) {
            log.error("Error during BMSLG seat booking", e);
            throw new RuntimeException("Failed to book seat: " + e.getMessage(), e);
        }
    }

    /**
     * Creates passenger data structure as per PHP script
     */
    private Map<String, Object> createPassengersData() {
        Map<String, Object> passengersMainData = new HashMap<>();
        passengersMainData.put("sub_id", "10");
        passengersMainData.put("ukey", "UKEYPHP123");
        passengersMainData.put("asi_id", "20");
        passengersMainData.put("from_id", "1");
        passengersMainData.put("to_id", "2");
        passengersMainData.put("trvl_dt", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        passengersMainData.put("tdi_id", "30");
        passengersMainData.put("lb_id", "40");
        passengersMainData.put("pbi_id", "50");
        passengersMainData.put("boarding", "Main Terminal PHP");
        passengersMainData.put("dropping", "Final Stop PHP");
        passengersMainData.put("boarding_time", "10:30");
        passengersMainData.put("dropping_time", "18:30");
        passengersMainData.put("apbi_id", "60");

        List<Map<String, Object>> passengers = new ArrayList<>();
        
        // Passenger 1
        Map<String, Object> passenger1 = new HashMap<>();
        passenger1.put("name", "Alice PHP");
        passenger1.put("gender", "Female");
        passenger1.put("category", "Adult");
        passenger1.put("passport", "");
        passenger1.put("seat_id", "B1");
        passenger1.put("new_seat_fare", "");
        passenger1.put("seat_mob", "");
        passenger1.put("email", "");
        passengers.add(passenger1);

        // Passenger 2
        Map<String, Object> passenger2 = new HashMap<>();
        passenger2.put("name", "Bob PHP");
        passenger2.put("gender", "Male");
        passenger2.put("category", "Adult");
        passenger2.put("passport", "P12345");
        passenger2.put("seat_id", "B2");
        passenger2.put("new_seat_fare", "15000");
        passenger2.put("seat_mob", "255700000001");
        passenger2.put("email", "bob.php@example.com");
        passengers.add(passenger2);

        passengersMainData.put("passengers", passengers);
        return passengersMainData;
    }

    /**
     * Populate passengers from BMSLG data
     */
    private Booking populatePassengersFromBmslgData(Booking booking, Map<String, Object> passengersMainData) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> passengersData = (List<Map<String, Object>>) passengersMainData.get("passengers");
        
        if (passengersData != null) {
            for (Map<String, Object> passengerData : passengersData) {
                Passenger passenger = new Passenger();
                
                // Personal details
                passenger.setFullName((String) passengerData.get("name"));
                passenger.setGender(Gender.valueOf(((String) passengerData.get("gender")).toUpperCase()));
                passenger.setCategory(PassengerCategory.valueOf(((String) passengerData.get("category")).toUpperCase()));
                passenger.setPassportNumber((String) passengerData.get("passport"));
                passenger.setPhoneNumber((String) passengerData.get("seat_mob"));
                passenger.setEmail((String) passengerData.get("email"));
                
                // Route information from main data
                passenger.setBoardingPoint((String) passengersMainData.get("boarding"));
                passenger.setDroppingPoint((String) passengersMainData.get("dropping"));
                passenger.setBoardingTime(java.time.LocalTime.parse((String) passengersMainData.get("boarding_time")));
                passenger.setDroppingTime(java.time.LocalTime.parse((String) passengersMainData.get("dropping_time")));
                
                // Seat and fare
                passenger.setSeatId((String) passengerData.get("seat_id"));
                String fareStr = (String) passengerData.get("new_seat_fare");
                passenger.setIndividualFare(fareStr != null && !fareStr.isEmpty() ? 
                    new java.math.BigDecimal(fareStr) : java.math.BigDecimal.ZERO);
                
                // Ticket information
                passenger.setTicketStatus(TicketStatus.ACTIVE);
                passenger.setIsCancelled(false);
                passenger.setRefundStatus(com.obuspartners.modules.booking_management.domain.enums.RefundStatus.NONE);
                
                // Relationship
                passenger.setBooking(booking);
                
                booking.getPassengers().add(passenger);
            }
        }

        return booking;
    }

    /**
     * Encrypts a string using the specified algorithm
     *
     * @param algorithmType Algorithm type (1=MD5, 2=SHA1, 3=SHA256, 4=SHA512)
     * @param input The string to encrypt
     * @return Encrypted string or null if algorithm not supported
     */
    private String encrypt(int algorithmType, String input) {
        if (input == null) {
            return null;
        }

        try {
            MessageDigest digest;
            switch (algorithmType) {
                case 1:
                    digest = MessageDigest.getInstance("MD5");
                    break;
                case 2:
                    digest = MessageDigest.getInstance("SHA-1");
                    break;
                case 3:
                    digest = MessageDigest.getInstance("SHA-256");
                    break;
                case 4:
                    digest = MessageDigest.getInstance("SHA-512");
                    break;
                default:
                    log.warn("Unsupported algorithm type: {}", algorithmType);
                    return null;
            }

            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            log.error("Algorithm not supported: {}", algorithmType, e);
            return null;
        }
    }

    /**
     * Helper method to generate MD5 hash
     */
    private String md5(String input) {
        return encrypt(1, input);
    }

    /**
     * Helper method to generate SHA-512 hash
     */
    private String sha512(String input) {
        return encrypt(4, input);
    }

    
}
