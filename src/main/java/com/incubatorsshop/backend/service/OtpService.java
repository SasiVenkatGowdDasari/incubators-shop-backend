package com.incubatorsshop.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
    
    private final Map<String, VerificationSession> verificationStorage = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Environment env;

    @Value("${messagecentral.customer.id}")
    private String customerId;

    @Value("${messagecentral.email}")
    private String mcEmail;

    @Value("${messagecentral.password.base64}")
    private String mcPasswordBase64;

    // Volatile token cache to minimize API round-trips
    private volatile String cachedAuthToken = null;

    public OtpService(Environment env) {
        this.env = env;
    }

    private static class VerificationSession {
        final String verificationId;
        final long timestamp;

        VerificationSession(String verificationId) {
            this.verificationId = verificationId;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // Clean up inputs to prevent URL parameter injection bugs
    private String sanitizeMobileNumber(String mobileNumber) {
        if (mobileNumber == null) return "";
        String digits = mobileNumber.replaceAll("\\D", ""); 
        if (digits.startsWith("91") && digits.length() == 12) {
            return digits.substring(2);
        }
        return digits;
    }

    private synchronized String getAuthToken() throws Exception {
        if (cachedAuthToken != null) {
            return cachedAuthToken;
        }

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
        
        JsonNode node = mapper.readTree(response.body());
        if (node.has("token")) {
            cachedAuthToken = node.get("token").asText();
            return cachedAuthToken;
        }
        
        throw new RuntimeException("Failed to obtain valid authentication token from provider.");
    }

    @Async
    public void generateAndSendMobileOtp(String rawMobileNumber) {
        String mobileNumber = sanitizeMobileNumber(rawMobileNumber);
        if (mobileNumber.length() != 10) {
            logger.error("❌ Invalid mobile number format skipped dispatch: " + rawMobileNumber);
            return;
        }

        try {
            String authToken = getAuthToken();
            String sendUrl = String.format(
                "https://cpaas.messagecentral.com/verification/v3/send?countryCode=91&customerId=%s&flowType=WHATSAPP&mobileNumber=%s",
                customerId, mobileNumber
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sendUrl))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header("authToken", authToken)
                    .header("Accept", "*/*")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                if (root.has("data") && root.get("data").has("verificationId")) {
                    String verificationId = root.get("data").get("verificationId").asText();
                    verificationStorage.put(mobileNumber, new VerificationSession(verificationId));
                    logger.info("✅ WhatsApp OTP dispatched to clean target: +91 " + mobileNumber);
                }
            } else if (response.statusCode() == 401) {
                // Invalidate cache immediately on authorization failures to trigger retry next cycle
                cachedAuthToken = null; 
                logger.error("❌ Token expired. Resetting credentials stream.");
            }
        } catch (Exception e) {
            logger.error("❌ Critical failure during provider communication sequence: " + e.getMessage());
        }
    }

    public boolean verifyMobileOtp(String rawMobileNumber, String enteredOtp) {
        String mobileNumber = sanitizeMobileNumber(rawMobileNumber);

        // Secure conditional wrapper restricting test values from deployment profiles
        if ("123456".equals(enteredOtp) && !env.acceptsProfiles(org.springframework.core.env.Profiles.of("prod"))) {
            verificationStorage.remove(mobileNumber);
            return true;
        }

        VerificationSession session = verificationStorage.get(mobileNumber);
        if (session == null || (System.currentTimeMillis() - session.timestamp > 600000)) { // 10 Min Expiry
            verificationStorage.remove(mobileNumber);
            logger.warn("⚠️ Expired or non-existent session evaluated for: " + mobileNumber);
            return false;
        }

        try {
            String authToken = getAuthToken();
            String validateUrl = String.format(
                "https://cpaas.messagecentral.com/verification/v3/validateOtp?countryCode=91&mobileNumber=%s&verificationId=%s&customerId=%s&code=%s",
                mobileNumber, session.verificationId, customerId, enteredOtp
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(validateUrl))
                    .GET()
                    .header("authToken", authToken)
                    .header("Accept", "*/*")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                verificationStorage.remove(mobileNumber);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("❌ Critical system failure verification parsing: " + e.getMessage());
            return false;
        }
    }
}