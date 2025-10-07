package com.obuspartners.modules.partner_integration.bmslg.agent_v8.buses;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.obuspartners.modules.partner_integration.bmslg.agent_v8.dto.BmsLgSearchBusRequestDto;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for searching bus data from BMSLG API.
 * 
 * This service implements the Search-Buses functionality as specified in the PHP script,
 * including proper authentication key generation and validation.
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class BmsLgBusServiceImpl implements BmsLgBusService {

    private static final String BMSLG_SEARCH_BUSES_URL = "https://bms.oacl.co.tz/api/Agents-V8/Search-Buses.php";
    private static final String BUS_SALT = "5e@rcH";
    private static final String BUS_POST_SALT = "5E@rCH";

    @Override
    public Object searchBuses() {
        log.info("Searching buses data from BMSLG");
        try {
            // Generate random auth key (1-5, but we removed RIPEMD160 support, so 1-4)
            Random random = new Random();
            int authKey = random.nextInt(4) + 1; // Random between 1-4

            // Prepare POST data as per PHP script
            Map<String, String> postData = new HashMap<>();
            postData.put("owner_id", "64");
            postData.put("agent_id", "3643");
            postData.put("auth_key", String.valueOf(authKey));
            postData.put("imei", "123456789012345");
            postData.put("lat", "0.0");
            postData.put("long", "0.0");
            postData.put("ip", "192.168.0.101");
            postData.put("from_id", "52"); // ABC, Tanzania
            postData.put("to_id", "54"); // XYZ, Mozambique
            postData.put("trvl_dt", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))); // Tomorrow's date
            postData.put("key", ""); // Will be calculated
            postData.put("app_ver", "4.88");
            postData.put("is_from", "4");
            postData.put("pltfm", "0"); // 0=ANDROID AGENT NORMAL, 1=ANDROID AGENT SPECIAL, etc.
            postData.put("lang", "0"); // 0=English, 1=Swahili

            // Prepare GET data
            Map<String, String> getData = new HashMap<>();
            getData.put("key", ""); // Will be calculated

            // Calculate GET key: encrypt(auth_key, md5(sha512(agent_id + owner_id + auth_key + "5e@rcH")))
            String getKeyInput = postData.get("agent_id") + postData.get("owner_id") + postData.get("auth_key") + BUS_SALT;
            String getKeyString = md5(sha512(getKeyInput));
            String getKey = encrypt(authKey, getKeyString);
            getData.put("key", getKey);

            // Calculate POST key: encrypt(auth_key, md5(sha512(owner_id + agent_id + from_id + to_id + trvl_dt + is_from + pltfm + lang + imei + lat + long + ip + auth_key + "5E@rCH")))
            String postKeyInput = postData.get("owner_id") + postData.get("agent_id") + postData.get("from_id") + 
                                postData.get("to_id") + postData.get("trvl_dt") + postData.get("is_from") + 
                                postData.get("pltfm") + postData.get("lang") + postData.get("imei") + 
                                postData.get("lat") + postData.get("long") + postData.get("ip") + 
                                postData.get("auth_key") + BUS_POST_SALT;
            String postKeyString = md5(sha512(postKeyInput));
            String postKey = encrypt(authKey, postKeyString);
            postData.put("key", postKey);

            // Build URL with GET parameters
            StringBuilder urlBuilder = new StringBuilder(BMSLG_SEARCH_BUSES_URL);
            urlBuilder.append("?");
            getData.forEach((key, value) ->
                urlBuilder.append(key).append("=").append(value).append("&"));
            String url = urlBuilder.toString().replaceAll("&$", "");

            // Build POST body
            StringBuilder postBody = new StringBuilder();
            postData.forEach((key, value) ->
                postBody.append(key).append("=").append(value).append("&"));
            String postBodyStr = postBody.toString().replaceAll("&$", "");

            log.info("BMSLG Search Buses URL: {}", url);
            log.info("BMSLG Search Buses Payload: {}", postBodyStr);

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postBodyStr))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("BMSLG Search Buses HTTP Code: {}", response.statusCode());
            log.info("BMSLG Search Buses Response: {}", response.body());

            return response.body();

        } catch (IOException | InterruptedException e) {
            log.error("Error during BMSLG buses search", e);
            throw new RuntimeException("Failed to search buses: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "busSearchCache", key = "#requestDto.ownerId + '_' + #requestDto.agentId + '_' + #requestDto.fromId + '_' + #requestDto.toId + '_' + #requestDto.trvlDt")
    public Object searchBuses(BmsLgSearchBusRequestDto requestDto) {
        log.info("Cache miss or expired, searching buses data from BMSLG with request parameters");
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
            postData.put("from_id", requestDto.getFromId() != null ? requestDto.getFromId() : "52");
            postData.put("to_id", requestDto.getToId() != null ? requestDto.getToId() : "54");
            postData.put("trvl_dt", requestDto.getTrvlDt() != null ? requestDto.getTrvlDt() : LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            postData.put("key", ""); // Will be calculated
            postData.put("app_ver", requestDto.getAppVer() != null ? requestDto.getAppVer() : "5.00");
            postData.put("is_from", requestDto.getIsFrom() != null ? requestDto.getIsFrom() : "4");
            postData.put("pltfm", requestDto.getPltfm() != null ? requestDto.getPltfm() : "0");
            postData.put("lang", requestDto.getLang() != null ? requestDto.getLang() : "1");

            // Prepare GET data
            Map<String, String> getData = new HashMap<>();
            getData.put("key", ""); // Will be calculated

            // Calculate GET key: encrypt(auth_key, md5(sha512(agent_id + owner_id + auth_key + "5e@rcH")))
            String getKeyInput = postData.get("agent_id") + postData.get("owner_id") + postData.get("auth_key") + BUS_SALT;
            String getKeyString = md5(sha512(getKeyInput));
            String getKey = encrypt(authKey, getKeyString);
            getData.put("key", getKey);

            // Calculate POST key: encrypt(auth_key, md5(sha512(owner_id + agent_id + from_id + to_id + trvl_dt + is_from + pltfm + lang + imei + lat + long + ip + auth_key + "5E@rCH")))
            String postKeyInput = postData.get("owner_id") + postData.get("agent_id") + postData.get("from_id") + 
                                postData.get("to_id") + postData.get("trvl_dt") + postData.get("is_from") + 
                                postData.get("pltfm") + postData.get("lang") + postData.get("imei") + 
                                postData.get("lat") + postData.get("long") + postData.get("ip") + 
                                postData.get("auth_key") + BUS_POST_SALT;
            String postKeyString = md5(sha512(postKeyInput));
            String postKey = encrypt(authKey, postKeyString);
            postData.put("key", postKey);

            // Build URL with GET parameters
            StringBuilder urlBuilder = new StringBuilder(BMSLG_SEARCH_BUSES_URL);
            urlBuilder.append("?");
            getData.forEach((key, value) ->
                urlBuilder.append(key).append("=").append(value).append("&"));
            String url = urlBuilder.toString().replaceAll("&$", "");

            // Build POST body
            StringBuilder postBody = new StringBuilder();
            postData.forEach((key, value) ->
                postBody.append(key).append("=").append(value).append("&"));
            String postBodyStr = postBody.toString().replaceAll("&$", "");

            log.info("BMSLG Search Buses URL: {}", url);
            log.info("BMSLG Search Buses Payload: {}", postBodyStr);

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postBodyStr))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("BMSLG Search Buses HTTP Code: {}", response.statusCode());
            log.info("BMSLG Search Buses Response: {}", response.body());

            return response.body();

        } catch (IOException | InterruptedException e) {
            log.error("Error during BMSLG buses search", e);
            throw new RuntimeException("Failed to search buses: " + e.getMessage(), e);
        }
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
