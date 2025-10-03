package com.obuspartners.modules.partner_integration.bmslg.agent_v8.seat;

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
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgProcessSeatRequestDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for processing seat data from BMSLG API.
 * 
 * This service implements the Process-Seat functionality as specified in the PHP script,
 * including proper authentication key generation, validation, and comprehensive field validation.
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class BmsLgSeatServiceImpl implements BmsLgSeatService {

    private static final String BMSLG_PROCESS_SEAT_URL = "https://bms.oacl.co.tz/api/Agents-V8/Process-Seat.php";
    private static final String SEAT_SALT = "pr0cE5$";
    private static final String SEAT_POST_SALT = "P5e@t";
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern SEAT_ID_PATTERN = Pattern.compile("^[A-Z0-9-]+$");
    private static final Pattern UKEY_PATTERN = Pattern.compile("^[A-Z0-9]+$");

    @Override
    public Object processSeat(BmsLgProcessSeatRequestDto requestDto) {
        log.info("Processing seat data from BMSLG");
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
            postData.put("owner_id", requestDto.getOwnerId());
            postData.put("agent_id", requestDto.getAgentId());
            postData.put("auth_key", String.valueOf(authKey));
            postData.put("imei", requestDto.getImei());
            postData.put("lat", requestDto.getLat());
            postData.put("long", requestDto.getLongitude());
            postData.put("ip", requestDto.getIp());
            postData.put("from_id", requestDto.getFromId());
            postData.put("to_id", requestDto.getToId());
            postData.put("trvl_dt", requestDto.getTrvlDt());
            postData.put("sub_id", requestDto.getSubId());
            postData.put("tdi_id", requestDto.getTdiId());
            postData.put("lb_id", requestDto.getLbId());
            postData.put("pbi_id", requestDto.getPbiId());
            postData.put("asi_id", requestDto.getAsiId());
            postData.put("apbi_id", requestDto.getApbiId());
            postData.put("seat_id", requestDto.getSeatId());
            postData.put("currency", requestDto.getCurrency());
            postData.put("seat_type_id", requestDto.getSeatTypeId());
            postData.put("ukey", requestDto.getUkey());
            postData.put("key", ""); // Will be calculated
            postData.put("app_ver", requestDto.getAppVer());
            postData.put("is_from", requestDto.getIsFrom());
            postData.put("pltfm", requestDto.getPltfm());
            postData.put("lang", requestDto.getLang());

            // Validate all fields
            List<String> validationErrors = validateFields(postData);
            if (!validationErrors.isEmpty()) {
                String errorMessage = "Validation errors: " + String.join(", ", validationErrors);
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }

            // Prepare GET data
            Map<String, String> getData = new HashMap<>();
            getData.put("key", ""); // Will be calculated

            // Calculate GET key: encrypt(auth_key, md5(sha512(agent_id + owner_id + auth_key + "pr0cE5$")))
            String getKeyInput = postData.get("agent_id") + postData.get("owner_id") + postData.get("auth_key") + SEAT_SALT;
            String getKeyString = md5(sha512(getKeyInput));
            String getKey = encrypt(authKey, getKeyString);
            getData.put("key", getKey);

            // Calculate POST key: encrypt(auth_key, md5(sha512(owner_id + agent_id + from_id + to_id + trvl_dt + sub_id + tdi_id + lb_id + pbi_id + asi_id + seat_id + currency + seat_type_id + ukey + imei + lat + long + ip + auth_key + is_from + pltfm + lang + apbi_id + "P5e@t")))
            String postKeyInput = postData.get("owner_id") + postData.get("agent_id") + postData.get("from_id") + 
                                postData.get("to_id") + postData.get("trvl_dt") + postData.get("sub_id") + 
                                postData.get("tdi_id") + postData.get("lb_id") + postData.get("pbi_id") + 
                                postData.get("asi_id") + postData.get("seat_id") + postData.get("currency") + 
                                postData.get("seat_type_id") + postData.get("ukey") + postData.get("imei") + 
                                postData.get("lat") + postData.get("long") + postData.get("ip") + 
                                postData.get("auth_key") + postData.get("is_from") + postData.get("pltfm") + 
                                postData.get("lang") + postData.get("apbi_id") + SEAT_POST_SALT;
            String postKeyString = md5(sha512(postKeyInput));
            String postKey = encrypt(authKey, postKeyString);
            postData.put("key", postKey);

            // Build URL with GET parameters
            StringBuilder urlBuilder = new StringBuilder(BMSLG_PROCESS_SEAT_URL);
            urlBuilder.append("?");
            getData.forEach((key, value) ->
                urlBuilder.append(key).append("=").append(value).append("&"));
            String url = urlBuilder.toString().replaceAll("&$", "");

            // Build POST body
            StringBuilder postBody = new StringBuilder();
            postData.forEach((key, value) ->
                postBody.append(key).append("=").append(value).append("&"));
            String postBodyStr = postBody.toString().replaceAll("&$", "");

            log.info("BMSLG Process Seat URL: {}", url);
            log.info("BMSLG Process Seat Payload: {}", postBodyStr);

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postBodyStr))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("BMSLG Process Seat HTTP Code: {}", response.statusCode());
            log.info("BMSLG Process Seat Response: {}", response.body());

            return response.body();

        } catch (IOException | InterruptedException e) {
            log.error("Error during BMSLG seat processing", e);
            throw new RuntimeException("Failed to process seat: " + e.getMessage(), e);
        }
    }

    /**
     * Validates all fields as per PHP script requirements
     *
     * @param postData The POST data to validate
     * @return List of validation errors (empty if all valid)
     */
    private List<String> validateFields(Map<String, String> postData) {
        List<String> errors = new ArrayList<>();
        
        // Numeric fields validation
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
        numericFields.put("from_id", Map.of("min", 1));
        numericFields.put("to_id", Map.of("min", 1));
        numericFields.put("currency", Map.of("min", 0));
        numericFields.put("seat_type_id", Map.of("min", 0));

        for (Map.Entry<String, Map<String, Integer>> entry : numericFields.entrySet()) {
            String field = entry.getKey();
            Map<String, Integer> rules = entry.getValue();
            
            if (!postData.containsKey(field)) {
                errors.add("Missing field: " + field);
                continue;
            }
            
            if (!NUMERIC_PATTERN.matcher(postData.get(field)).matches()) {
                errors.add("Invalid format for " + field + " (must be numeric)");
                continue;
            }
            
            int value = Integer.parseInt(postData.get(field));
            if (value < rules.get("min") || (rules.containsKey("max") && value > rules.get("max"))) {
                errors.add("Value out of range for " + field);
            }
        }

        // Special format validations
        if (!postData.containsKey("seat_id")) {
            errors.add("Missing field: seat_id");
        } else if (!SEAT_ID_PATTERN.matcher(postData.get("seat_id")).matches()) {
            errors.add("Invalid format for seat_id");
        }

        if (!postData.containsKey("ukey")) {
            errors.add("Missing field: ukey");
        } else if (!UKEY_PATTERN.matcher(postData.get("ukey")).matches()) {
            errors.add("Invalid format for ukey");
        }

        if (!postData.containsKey("trvl_dt")) {
            errors.add("Missing field: trvl_dt");
        } else {
            try {
                LocalDate.parse(postData.get("trvl_dt"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                errors.add("Invalid date format for trvl_dt (must be YYYY-MM-DD)");
            }
        }
        
        return errors;
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
