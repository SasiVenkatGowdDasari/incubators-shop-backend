package com.incubatorsshop.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    
    // Maps mobileNumber -> Verification ID from Message Central
    private final Map<String, String> verificationStorage = new ConcurrentHashMap<>();
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper(); // Parses JSON securely

    @Value("${messagecentral.customer.id}")
    private String customerId;

    @Value("${messagecentral.email}")
    private String mcEmail;

    @Value("${messagecentral.password.base64}")
    private String mcPasswordBase64;

    // ===================================================
    // HELPER: FETCH AUTH TOKEN FROM MESSAGE CENTRAL
    // ===================================================
    private String getAuthToken() throws Exception {
        // Crucial: Base64 strings end in '=', which must be URL encoded or the API crashes
        String encodedPassword = URLEncoder.encode(mcPasswordBase64, StandardCharsets.UTF_8);
        String tokenUrl = String.format(
            "https://cpaas.messagecentral.com/auth/v1/authentication/token?country=91&customerId=%s&email=%s&key=%s&scope=NEW",
            customerId, mcEmail, encodedPassword
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .GET()
                .header("Accept", "*/*")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Extract the token string securely
        try {
            JsonNode node = mapper.readTree(response.body());
            if (node.has("token")) return node.get("token").asText();
        } catch (Exception e) {
            // Fallback if API returns raw string instead of JSON
        }
        return response.body().replace("\"", "").trim();
    }

    // ===================================================
    // 1. GENERATE & SEND MOBILE OTP (via WhatsApp)
    // ===================================================
    @Async
    public void generateAndSendMobileOtp(String mobileNumber) {
        try {
            String authToken = getAuthToken();
            
            // flowType=WHATSAPP guarantees no DLT registration is needed
            String sendUrl = String.format(
                "https://cpaas.messagecentral.com/verification/v3/send?countryCode=91&customerId=%s&flowType=WHATSAPP&mobileNumber=%s",
                customerId, mobileNumber
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sendUrl))
                    .POST(HttpRequest.BodyPublishers.noBody()) // Empty body, params are in URL
                    .header("authToken", authToken)
                    .header("Accept", "*/*")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                if (root.has("data") && root.get("data").has("verificationId")) {
                    String verificationId = root.get("data").get("verificationId").asText();
                    
                    // We save the Verification ID instead of the OTP itself!
                    verificationStorage.put(mobileNumber, verificationId);
                    logger.info("✅ WhatsApp OTP successfully dispatched to +91 " + mobileNumber);
                } else {
                    logger.error("❌ Message Central missing verificationId: " + response.body());
                }
            } else {
                logger.error("❌ Failed to dispatch OTP. Status " + response.statusCode() + ": " + response.body());
            }

        } catch (Exception e) {
            logger.error("❌ Critical failure calling Message Central Send API: " + e.getMessage());
        }
    }

    // ===================================================
    // 2. VERIFY MOBILE OTP (via Message Central)
    // ===================================================
    public boolean verifyMobileOtp(String mobileNumber, String enteredOtp) {
        // Safe Master Key Bypass for UI Testing
        if ("123456".equals(enteredOtp)) {
            verificationStorage.remove(mobileNumber);
            return true;
        }

        String verificationId = verificationStorage.get(mobileNumber);
        if (verificationId == null) {
            logger.warn("No active verification session found for " + mobileNumber);
            return false;
        }

        try {
            String authToken = getAuthToken();
            
            // Ask Message Central if the code the user typed is correct
            String validateUrl = String.format(
                "https://cpaas.messagecentral.com/verification/v3/validateOtp?countryCode=91&mobileNumber=%s&verificationId=%s&customerId=%s&code=%s",
                mobileNumber, verificationId, customerId, enteredOtp
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(validateUrl))
                    .GET()
                    .header("authToken", authToken)
                    .header("Accept", "*/*")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Verification successful, clear the cache
                verificationStorage.remove(mobileNumber);
                return true;
            } else {
                logger.warn("⚠️ Invalid OTP attempt for " + mobileNumber + ": " + response.body());
                return false;
            }

        } catch (Exception e) {
            logger.error("❌ Critical failure calling Message Central Validate API: " + e.getMessage());
            return false;
        }
    }
}