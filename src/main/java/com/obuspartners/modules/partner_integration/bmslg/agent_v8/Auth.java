package com.obuspartners.modules.partner_integration.bmslg.agent_v8;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

/**
 * BMSLG Authentication Service
 * Handles authentication with BMSLG (Bus Management System Login Gateway)
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class Auth {

    private static final String BMSLG_SIGN_IN_URL = "https://bms.oacl.co.tz/api/Agents-V8/Sign-In.php";
    private static final String LOGIN_SALT = "l0g!n";
    private static final String POST_SALT = "5mS!0g1n";

    /**
     * Encrypts a string using the specified algorithm
     * 
     * @param algorithmType Algorithm type (1=MD5, 2=SHA1, 3=SHA256, 4=SHA512)
     * @param input The string to encrypt
     * @return Encrypted string or null if algorithm not supported
     */
    public String encrypt(int algorithmType, String input) {
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
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
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
            log.error("MD5 algorithm not supported", e);
            return null;
        }
    }

    /**
     * Helper method to generate SHA512 hash
     */
    private String sha512(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
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
            log.error("SHA-512 algorithm not supported", e);
            return null;
        }
    }

    /**
     * Creates authentication request data
     * 
     * @param username Agent username
     * @param password Agent password
     * @param imei Device IMEI
     * @param latitude Latitude
     * @param longitude Longitude
     * @param ipAddress IP address
     * @param ownerId Owner ID
     * @param authKey Authentication key (1-4)
     * @param isFrom Source identifier
     * @param platform Platform type (0=ANDROID AGENT NORMAL, 1=ANDROID AGENT SPECIAL, 2=ANDROID AGENT SHORT, 3=ANDROID AGENT CARGO)
     * @param language Language (0=English, 1=Swahili)
     * @param appVersion Application version
     * @return Authentication request data
     */
    public AuthRequestData createAuthRequest(String username, String password, String imei, 
                                           String latitude, String longitude, String ipAddress,
                                           String ownerId, int authKey, String isFrom, 
                                           String platform, String language, String appVersion) {
        
        // Validate required fields
        Map<String, String> requiredFields = new HashMap<>();
        requiredFields.put("username", username);
        requiredFields.put("password", password);
        requiredFields.put("imei", imei);
        requiredFields.put("latitude", latitude);
        requiredFields.put("longitude", longitude);
        requiredFields.put("ip", ipAddress);
        requiredFields.put("owner_id", ownerId);
        requiredFields.put("is_from", isFrom);
        requiredFields.put("platform", platform);
        requiredFields.put("language", language);
        requiredFields.put("app_version", appVersion);

        String missingFields = requiredFields.entrySet().stream()
            .filter(entry -> entry.getValue() == null || entry.getValue().trim().isEmpty())
            .map(Map.Entry::getKey)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Missing required fields: " + missingFields);
        }

        // Generate GET key - match BMSLG server logic: encrpt(auth_key, md5(hash("sha512", owner_id + auth_key + "l0g!n")))
        String getKeyInput = ownerId + authKey + LOGIN_SALT;
        String getKey = encrypt(authKey, md5(sha512(getKeyInput)));

        // Encrypt password - match Dart implementation: SHA1 -> SHA512 -> authKey
        String hashedPassword = password;
        // First apply algorithm 2 (SHA1)
        hashedPassword = encrypt(2, hashedPassword);
        // Then apply algorithm 4 (SHA512)
        hashedPassword = encrypt(4, hashedPassword);
        // Finally apply the authKey algorithm
        String encryptedPassword = encrypt(authKey, hashedPassword);

        // Create raw string for POST key
        String rawString = username + encryptedPassword + isFrom + platform + language +
                          imei + latitude + longitude + ipAddress + ownerId + authKey + POST_SALT;

        // Generate POST key - match BMSLG server logic: encrpt(auth_key, md5(hash("sha512", rawString)))
        String postKey = encrypt(authKey, md5(sha512(rawString)));

        return new AuthRequestData(getKey, encryptedPassword, postKey);
    }

    /**
     * Sends authentication request to BMSLG
     * 
     * @param username Agent username
     * @param password Agent password
     * @param imei Device IMEI
     * @param latitude Latitude
     * @param longitude Longitude
     * @param ipAddress IP address
     * @param ownerId Owner ID
     * @param authKey Authentication key (1-4)
     * @param isFrom Source identifier
     * @param platform Platform type
     * @param language Language
     * @param appVersion Application version
     * @return Authentication response
     */
    public AuthResponse authenticate(String username, String password, String imei,
                                  String latitude, String longitude, String ipAddress,
                                  String ownerId, int authKey, String isFrom,
                                  String platform, String language, String appVersion) {
        
        try {
            AuthRequestData authData = createAuthRequest(username, password, imei, latitude, 
                                                       longitude, ipAddress, ownerId, authKey, 
                                                       isFrom, platform, language, appVersion);

            // Prepare POST data
            Map<String, String> postData = new HashMap<>();
            postData.put("username", username);
            postData.put("pswd", authData.getEncryptedPassword());
            postData.put("imei", imei);
            postData.put("lat", latitude);
            postData.put("long", longitude);
            postData.put("ip", ipAddress);
            postData.put("owner_id", ownerId);
            postData.put("auth_key", String.valueOf(authKey));
            postData.put("key", authData.getPostKey());
            postData.put("is_from", isFrom);
            postData.put("pltfm", platform);
            postData.put("lang", language);
            postData.put("app_ver", appVersion);

            // Prepare GET parameters
            Map<String, String> getData = new HashMap<>();
            getData.put("key", authData.getGetKey());

            // Build URL with GET parameters
            StringBuilder urlBuilder = new StringBuilder(BMSLG_SIGN_IN_URL);
            urlBuilder.append("?");
            getData.forEach((key, value) -> 
                urlBuilder.append(key).append("=").append(value).append("&"));
            String url = urlBuilder.toString().replaceAll("&$", "");

            // Build POST body
            StringBuilder postBody = new StringBuilder();
            postData.forEach((key, value) -> 
                postBody.append(key).append("=").append(value).append("&"));
            String postBodyStr = postBody.toString().replaceAll("&$", "");

            log.info("BMSLG Auth URL: {}", url);
            log.info("BMSLG Auth Payload: {}", postBodyStr);

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postBodyStr))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("BMSLG Auth HTTP Code: {}", response.statusCode());
            log.info("BMSLG Auth Response: {}", response.body());

            return new AuthResponse(response.statusCode(), response.body());

        } catch (IOException | InterruptedException e) {
            log.error("Error during BMSLG authentication", e);
            return new AuthResponse(500, "Authentication request failed: " + e.getMessage());
        }
    }

    /**
     * Convenience method for authentication with default values
     */
    public AuthResponse authenticateWithDefaults(String username, String password, String ownerId) {
        Random random = new Random();
        int authKey = random.nextInt(4) + 1; // Random between 1-4

        return authenticate(username, password, "123456789012345", "0.0", "0.0", 
                          "192.168.0.101", ownerId, authKey, "4", "0", "0", "4.88");
    }

    /**
     * Data class for authentication request
     */
    public static class AuthRequestData {
        private final String getKey;
        private final String encryptedPassword;
        private final String postKey;

        public AuthRequestData(String getKey, String encryptedPassword, String postKey) {
            this.getKey = getKey;
            this.encryptedPassword = encryptedPassword;
            this.postKey = postKey;
        }

        public String getGetKey() { return getKey; }
        public String getEncryptedPassword() { return encryptedPassword; }
        public String getPostKey() { return postKey; }
    }

    /**
     * Data class for authentication response
     */
    public static class AuthResponse {
        private final int httpCode;
        private final String responseBody;

        public AuthResponse(int httpCode, String responseBody) {
            this.httpCode = httpCode;
            this.responseBody = responseBody;
        }

        public int getHttpCode() { return httpCode; }
        public String getResponseBody() { return responseBody; }
        public boolean isSuccess() { return httpCode >= 200 && httpCode < 300; }
    }
}
