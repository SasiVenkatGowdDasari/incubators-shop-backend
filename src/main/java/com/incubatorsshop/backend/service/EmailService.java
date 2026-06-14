package com.incubatorsshop.backend.service;

import com.incubatorsshop.backend.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${resend.api.key}")
    private String resendApiKey;

    // This MUST match the email you used to sign up for Resend
    @Value("${incubators.shop.admin-email:sasivenkatgowd5451@gmail.com}")
    private String adminEmail;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private static final String SENDER_EMAIL = "Venkat Incubators <onboarding@resend.dev>";

    // ==========================================
    // ONLY ADMIN GETS EMAILS (100% Free Sandbox Mode)
    // ==========================================
    @Async
    public void sendOrderNotificationToAdmin(Order order) {
        try {
            String subject = "🚨 NEW ORDER! #" + order.getId() + " - " + order.getProduct().getTitle();
            
            // Handle null emails gracefully since customers only use mobile now
            String customerEmail = (order.getUser().getEmail() != null && !order.getUser().getEmail().trim().isEmpty()) 
                                   ? order.getUser().getEmail() 
                                   : "Not Provided (Mobile User)";

            String htmlContent = "<h2>New Order Alert</h2>" 
                    + "<p><strong>Customer Name:</strong> " + order.getUser().getFullName() + "</p>" 
                    + "<p><strong>Mobile Number:</strong> +91 " + order.getUser().getMobileNumber() + "</p>" 
                    + "<p><strong>Customer Email:</strong> " + customerEmail + "</p>" 
                    + "<p><strong>Delivery Address:</strong> " + order.getUser().getAddress() + "</p>" 
                    + "<hr>" 
                    + "<h3>Order Details:</h3>"
                    + "<p><strong>Product:</strong> " + order.getProduct().getTitle() + "</p>"
                    + "<p><strong>Quantity:</strong> " + order.getQuantity() + "</p>"
                    + "<p><strong>Total Revenue:</strong> ₹" + order.getTotalPrice() + "</p>"
                    + "<br><p>Please log in to the Admin Dashboard to review and dispatch this order.</p>";

            String cleanHtml = htmlContent.replace("\"", "\\\"").replace("\n", "");

            String jsonPayload = """
                {
                    "from": "%s",
                    "to": ["%s"],
                    "subject": "%s",
                    "html": "%s"
                }
                """.formatted(SENDER_EMAIL, adminEmail, subject, cleanHtml);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                logger.info("✅ Admin Alert Email successfully sent for Order #" + order.getId());
            } else {
                logger.error("❌ Resend API Error: " + response.body());
            }

        } catch (Exception e) {
            logger.error("❌ Failed to send Admin Alert: " + e.getMessage());
        }
    }
}