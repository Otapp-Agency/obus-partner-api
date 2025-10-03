package com.obuspartners.modules.partner_integration.bmslg.agent_v8.booking;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

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
public class BmsLgBookSeatServiceImpl implements BmsLgBookSeatService {

    private static final String BMSLG_BOOK_SEAT_URL = "https://bms.oacl.co.tz/api/Agents-V8/Book-Seat.php";
    private static final String BOOK_SALT = "b0Ok";
    private static final String BOOK_POST_SALT = "B0Ok5e@t";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object bookSeat() {
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
