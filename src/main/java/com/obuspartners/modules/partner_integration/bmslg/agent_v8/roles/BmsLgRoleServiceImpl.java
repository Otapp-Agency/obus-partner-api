package com.obuspartners.modules.partner_integration.bmslg.agent_v8.roles;

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

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for fetching role data from BMSLG API.
 * 
 * This service implements the Get-Roles functionality as specified in the PHP script,
 * including proper authentication key generation and validation.
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class BmsLgRoleServiceImpl implements BmsLgRoleService {

    private static final String BMSLG_GET_ROLES_URL = "https://bms.oacl.co.tz/api/Agents-V8/Get-Roles.php";
    private static final String ROLE_SALT = "rO!e5";
    private static final String ROLE_POST_SALT = "Ro1e5";

    @Override
    public Object getRoles() {
        log.info("Fetching roles data from BMSLG");
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
            postData.put("key", ""); // Will be calculated
            postData.put("app_ver", "4.88");
            postData.put("is_from", "4");
            postData.put("pltfm", "0"); // 0=ANDROID AGENT NORMAL, 1=ANDROID AGENT SPECIAL, etc.
            postData.put("lang", "0"); // 0=English, 1=Swahili

            // Prepare GET data
            Map<String, String> getData = new HashMap<>();
            getData.put("key", ""); // Will be calculated

            // Calculate GET key: encrypt(auth_key, md5(sha512(agent_id + owner_id + auth_key + "rO!e5")))
            String getKeyInput = postData.get("agent_id") + postData.get("owner_id") + postData.get("auth_key") + ROLE_SALT;
            String getKeyString = md5(sha512(getKeyInput));
            String getKey = encrypt(authKey, getKeyString);
            getData.put("key", getKey);

            // Calculate POST key: encrypt(auth_key, md5(sha512(owner_id + agent_id + imei + lat + long + ip + is_from + pltfm + lang + auth_key + "Ro1e5")))
            String postKeyInput = postData.get("owner_id") + postData.get("agent_id") + postData.get("imei") + 
                                postData.get("lat") + postData.get("long") + postData.get("ip") + 
                                postData.get("is_from") + postData.get("pltfm") + postData.get("lang") + 
                                postData.get("auth_key") + ROLE_POST_SALT;
            String postKeyString = md5(sha512(postKeyInput));
            String postKey = encrypt(authKey, postKeyString);
            postData.put("key", postKey);

            // Build URL with GET parameters
            StringBuilder urlBuilder = new StringBuilder(BMSLG_GET_ROLES_URL);
            urlBuilder.append("?");
            getData.forEach((key, value) ->
                urlBuilder.append(key).append("=").append(value).append("&"));
            String url = urlBuilder.toString().replaceAll("&$", "");

            // Build POST body
            StringBuilder postBody = new StringBuilder();
            postData.forEach((key, value) ->
                postBody.append(key).append("=").append(value).append("&"));
            String postBodyStr = postBody.toString().replaceAll("&$", "");

            log.info("BMSLG Get Roles URL: {}", url);
            log.info("BMSLG Get Roles Payload: {}", postBodyStr);

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postBodyStr))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("BMSLG Get Roles HTTP Code: {}", response.statusCode());
            log.info("BMSLG Get Roles Response: {}", response.body());

            return response.body();

        } catch (IOException | InterruptedException e) {
            log.error("Error during BMSLG roles fetch", e);
            throw new RuntimeException("Failed to fetch roles: " + e.getMessage(), e);
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
