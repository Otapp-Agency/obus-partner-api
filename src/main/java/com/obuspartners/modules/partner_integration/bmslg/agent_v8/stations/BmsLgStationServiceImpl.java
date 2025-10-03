package com.obuspartners.modules.partner_integration.bmslg.agent_v8.stations;

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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for fetching station data from BMSLG API with caching support.
 * 
 * Features:
 * - 5-minute TTL cache to reduce API calls and improve performance
 * - Automatic cache expiration and refresh
 * - Manual cache clearing capability
 * - Cache statistics for monitoring
 * 
 * Cache behavior:
 * - First request: Fetches data from BMSLG API and caches it
 * - Subsequent requests within 5 minutes: Returns cached data
 * - After 5 minutes: Cache expires, next request fetches fresh data
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BmsLgStationServiceImpl implements BmsLgStationService {

    private static final String BMSLG_SEARCH_STATIONS_URL = "https://bms.oacl.co.tz/api/Agents-V8/Search-Stations.php";
    private static final String STATION_SALT = "5t@t!0n$";

    @Override
    @Cacheable(value = "stationCache", key = "'all_stations'")
    public Object fetchAllStations() {
        log.info("Cache miss or expired, fetching fresh station data from BMSLG");
        try {
            // Generate random auth key (1-5, but we removed RIPEMD160 support, so 1-4)
            Random random = new Random();
            int authKey = random.nextInt(4) + 1; // Random between 1-4

            // Prepare POST data
            Map<String, String> postData = new HashMap<>();
            postData.put("imei", "123456789012345");
            postData.put("lat", "0.0");
            postData.put("long", "0.0");
            postData.put("ip", "192.168.0.101");
            postData.put("owner_id", "64");
            postData.put("auth_key", String.valueOf(authKey));
            postData.put("key", ""); // Will be calculated
            postData.put("is_from", "4");
            postData.put("pltfm", "0"); // 0=ANDROID AGENT NORMAL, 1=ANDROID AGENT SPECIAL, 2=ANDROID AGENT SHORT, 3=ANDROID AGENT CARGO
            postData.put("lang", "0"); // 0=English, 1=Swahili
            postData.put("app_ver", "4.88");

            // Prepare GET data
            Map<String, String> getData = new HashMap<>();
            getData.put("key", ""); // Will be calculated

            // Calculate GET key: encrypt(auth_key, owner_id + auth_key + "5t@t!0n$")
            String getKeyInput = postData.get("owner_id") + postData.get("auth_key") + STATION_SALT;
            String getKey = encrypt(authKey, getKeyInput);
            getData.put("key", getKey);

            // Calculate POST key: encrypt(auth_key, raw_string)
            String rawString = postData.get("owner_id") + postData.get("is_from") + postData.get("pltfm") +
                              postData.get("lang") + postData.get("imei") + postData.get("lat") +
                              postData.get("long") + postData.get("ip") + postData.get("auth_key") + STATION_SALT;
            String postKey = encrypt(authKey, rawString);
            postData.put("key", postKey);

            // Build URL with GET parameters
            StringBuilder urlBuilder = new StringBuilder(BMSLG_SEARCH_STATIONS_URL);
            urlBuilder.append("?");
            getData.forEach((key, value) ->
                urlBuilder.append(key).append("=").append(value).append("&"));
            String url = urlBuilder.toString().replaceAll("&$", "");

            // Build POST body
            StringBuilder postBody = new StringBuilder();
            postData.forEach((key, value) ->
                postBody.append(key).append("=").append(value).append("&"));
            String postBodyStr = postBody.toString().replaceAll("&$", "");

            log.info("BMSLG Search Stations URL: {}", url);
            log.info("BMSLG Search Stations Payload: {}", postBodyStr);

            // Send HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postBodyStr))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("BMSLG Search Stations HTTP Code: {}", response.statusCode());
            log.info("BMSLG Search Stations Response: {}", response.body());

            return response.body();

        } catch (IOException | InterruptedException e) {
            log.error("Error during BMSLG stations fetch", e);
            throw new RuntimeException("Failed to fetch stations: " + e.getMessage(), e);
        }
    }

    /**
     * Clear the station cache manually
     * Useful for forcing a fresh fetch on next request
     */
    @CacheEvict(value = "stationCache", key = "'all_stations'")
    public void clearStationCache() {
        log.info("Station cache cleared manually");
    }

    /**
     * Get cache statistics for monitoring
     * @return Map containing cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheType", "Spring Cache (Redis or In-Memory)");
        stats.put("cacheName", "stationCache");
        stats.put("cacheKey", "all_stations");
        stats.put("ttlSeconds", 3600);
        stats.put("ttlMinutes", 60);
        stats.put("note", "Cache statistics depend on underlying cache implementation");
        return stats;
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
}
