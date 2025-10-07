package com.obuspartners.modules.partner_integration.bmslg.agent_v8.seat_map;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgSeatMapRequestDto;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for fetching seat map data from BMSLG API.
 * 
 * This service implements the Seat-Map functionality as specified in the PHP
 * script,
 * including proper authentication key generation, validation, and numeric field
 * validation.
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class BmsLgSeatMapServiceImpl implements BmsLgSeatMapService {

    private static final String BMSLG_SEAT_MAP_URL = "https://bms.oacl.co.tz/api/Agents-V8/Seat-Map.php";
    private static final String SEAT_MAP_SALT = "5e@tMp";
    private static final String SEAT_MAP_POST_SALT = "M@p5e@t";
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");

    @Override
    public Object getSeatMap(BmsLgSeatMapRequestDto requestDto) {
        log.info("Fetching seat map data from BMSLG with request parameters");
        try {
            // Use auth key from request or generate random one
            int authKey;
            if (requestDto.getAuthKey() != null && !requestDto.getAuthKey().isEmpty()) {
                authKey = Integer.parseInt(requestDto.getAuthKey());
            } else {
                Random random = new Random();
                authKey = random.nextInt(4) + 1; // Random between 1-4
            }

            // Prepare POST data from request DTO
            Map<String, String> postData = new HashMap<>();
            postData.put("owner_id", requestDto.getOwnerId() != null ? requestDto.getOwnerId() : "64");
            postData.put("agent_id", requestDto.getAgentId() != null ? requestDto.getAgentId() : "3643");
            postData.put("auth_key", String.valueOf(authKey));
            postData.put("imei", requestDto.getImei() != null ? requestDto.getImei() : "");
            postData.put("lat", requestDto.getLat() != null ? requestDto.getLat() : "0.0");
            postData.put("long", requestDto.getLongitude() != null ? requestDto.getLongitude() : "0.0");
            postData.put("ip", requestDto.getIp() != null ? requestDto.getIp() : "192.168.0.101");
            postData.put("sub_id", requestDto.getSubId() != null ? requestDto.getSubId() : "1454462");
            postData.put("tdi_id", requestDto.getTdiId() != null ? requestDto.getTdiId() : "367172190");
            postData.put("lb_id", requestDto.getLbId() != null ? requestDto.getLbId() : "9772");
            postData.put("pbi_id", requestDto.getPbiId() != null ? requestDto.getPbiId() : "125");
            postData.put("asi_id", requestDto.getAsiId() != null ? requestDto.getAsiId() : "364949203");
            postData.put("apbi_id", requestDto.getApbiId() != null ? requestDto.getApbiId() : "198519");
            postData.put("key", ""); // Will be calculated
            postData.put("app_ver", requestDto.getAppVer() != null ? requestDto.getAppVer() : "5.00");
            postData.put("is_from", requestDto.getIsFrom() != null ? requestDto.getIsFrom() : "4");
            postData.put("pltfm", requestDto.getPltfm() != null ? requestDto.getPltfm() : "0");
            postData.put("lang", requestDto.getLang() != null ? requestDto.getLang() : "1");

            // Validate numeric fields
            if (!validateNumericFields(postData)) {
                throw new RuntimeException("Invalid numeric field values");
            }

            // Prepare GET data
            Map<String, String> getData = new HashMap<>();
            getData.put("key", ""); // Will be calculated

            // Calculate GET key: encrypt(auth_key, md5(sha512(agent_id + owner_id +
            // auth_key + "5e@tMp")))
            String getKeyInput = postData.get("agent_id") + postData.get("owner_id") + postData.get("auth_key")
                    + SEAT_MAP_SALT;
            String getKeyString = md5(sha512(getKeyInput));
            String getKey = encrypt(authKey, getKeyString);
            getData.put("key", getKey);

            // Calculate POST key: encrypt(auth_key, md5(sha512(owner_id + agent_id + sub_id
            // + tdi_id + lb_id + pbi_id + asi_id + imei + lat + long + ip + auth_key +
            // is_from + pltfm + lang + apbi_id + "M@p5e@t")))
            String postKeyInput = postData.get("owner_id") + postData.get("agent_id") + postData.get("sub_id") +
                    postData.get("tdi_id") + postData.get("lb_id") + postData.get("pbi_id") +
                    postData.get("asi_id") + postData.get("imei") + postData.get("lat") +
                    postData.get("long") + postData.get("ip") + postData.get("auth_key") +
                    postData.get("is_from") + postData.get("pltfm") + postData.get("lang") +
                    postData.get("apbi_id") + SEAT_MAP_POST_SALT;
            String postKeyString = md5(sha512(postKeyInput));
            String postKey = encrypt(authKey, postKeyString);
            postData.put("key", postKey);

            // Build URL with GET parameters
            StringBuilder urlBuilder = new StringBuilder(BMSLG_SEAT_MAP_URL);
            urlBuilder.append("?");
            getData.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
            String url = urlBuilder.toString().replaceAll("&$", "");

            // Build POST body
            StringBuilder postBody = new StringBuilder();
            postData.forEach((key, value) -> postBody.append(key).append("=").append(value).append("&"));
            String postBodyStr = postBody.toString().replaceAll("&$", "");

            log.info("BMSLG Seat Map URL: {}", url);
            log.info("BMSLG Seat Map Payload: {}", postBodyStr);

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(postBodyStr))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("BMSLG Seat Map HTTP Code: {}", response.statusCode());
            log.info("BMSLG Seat Map Response: {}", response.body());

            return response.body();

        } catch (IOException | InterruptedException e) {
            log.error("Error during BMSLG seat map fetch", e);
            throw new RuntimeException("Failed to fetch seat map: " + e.getMessage(), e);
        }
    }

    /**
     * Validates numeric fields as per PHP script requirements
     *
     * @param postData The POST data to validate
     * @return true if all numeric fields are valid, false otherwise
     */
    private boolean validateNumericFields(Map<String, String> postData) {
        Map<String, Map<String, Integer>> numericFields = new HashMap<>();
        numericFields.put("sub_id", Map.of("min", 1));
        numericFields.put("tdi_id", Map.of("min", 1));
        numericFields.put("lb_id", Map.of("min", 1));
        numericFields.put("pbi_id", Map.of("min", 1));
        numericFields.put("asi_id", Map.of("min", 1));
        numericFields.put("owner_id", Map.of("min", 1));
        numericFields.put("agent_id", Map.of("min", 1));
        numericFields.put("apbi_id", Map.of("min", 1));
        numericFields.put("is_from", Map.of("min", 0, "max", 4));

        for (Map.Entry<String, Map<String, Integer>> entry : numericFields.entrySet()) {
            String field = entry.getKey();
            Map<String, Integer> rules = entry.getValue();

            if (!postData.containsKey(field) || !NUMERIC_PATTERN.matcher(postData.get(field)).matches()) {
                log.error("Field {} is missing or not numeric", field);
                return false;
            }

            int value = Integer.parseInt(postData.get(field));
            if (value < rules.get("min") || (rules.containsKey("max") && value > rules.get("max"))) {
                log.error("Field {} value {} is out of range (min: {}, max: {})",
                        field, value, rules.get("min"), rules.getOrDefault("max", Integer.MAX_VALUE));
                return false;
            }
        }

        return true;
    }

    /**
     * Encrypts a string using the specified algorithm
     *
     * @param algorithmType Algorithm type (1=MD5, 2=SHA1, 3=SHA256, 4=SHA512)
     * @param input         The string to encrypt
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
