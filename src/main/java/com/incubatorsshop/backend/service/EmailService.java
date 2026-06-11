package com.incubatorsshop.backend.service;

import com.incubatorsshop.backend.entity.Order;
import com.incubatorsshop.backend.entity.Product;
import com.incubatorsshop.backend.entity.User;
import com.incubatorsshop.backend.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    public EmailService(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    // ==========================================
    // 1. SEND EMAIL TO ADMIN
    // ==========================================
    @Async
    public void sendOrderNotificationToAdmin(Order order) {
        try {
            // Fetch the Admin dynamically from the Database
            Optional<User> adminOpt = userRepository.findFirstByRole("ADMIN");

            if (adminOpt.isEmpty() || adminOpt.get().getEmail() == null) {
                logger.warn("No Admin user with a valid email found in the database. Email skipped.");
                return;
            }

            String adminEmail = adminOpt.get().getEmail();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(adminEmail);
            helper.setSubject("New Order Placed! ID: #" + order.getId());

            String htmlContent = "<h2>New Order Alert</h2>" + "<p><strong>Customer Id:</strong> "
                    + order.getUser().getId() + "</p>" + "<p><strong>Customer Name:</strong> "
                    + order.getUser().getFullName() + "</p>" + "<p><strong>Customer Mobile:</strong> "
                    + order.getUser().getMobileNumber() + "</p>" + "<p><strong>Customer Email:</strong> "
                    + order.getUser().getEmail() + "</p>" + "<p><strong>Delivery Address:</strong> "
                    + order.getUser().getAddress() + "</p>" + "<hr>" + "<h3>Order Details:</h3>"
                    + "<p><strong>Product Id:</strong> " + order.getProduct().getId() + "</p>"
                    + "<p><strong>Product Name:</strong> " + order.getProduct().getTitle() + "</p>"
                    + "<p><strong>Capacity:</strong> " + order.getProduct().getCapacity() + "</p>"
                    + "<p><strong>Material:</strong> " + order.getProduct().getMaterial() + "</p>"
                    + "<p><strong>Shipping:</strong> " + order.getProduct().getShippingOptions() + "</p>"
                    + "<p><strong>Warranty:</strong> " + order.getProduct().getWarranty() + "</p>"
                    + "<p><strong>Description:</strong> " + order.getProduct().getDescription() + "</p>"
                    + "<p><strong>Quantity:</strong> " + order.getQuantity() + "</p>"
                    + "<p><strong>Total Revenue:</strong> ₹" + order.getTotalPrice() + "</p>"
                    + "<br><p>Please log in to the Admin Dashboard to review and dispatch this order.</p>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            logger.info("Order Notification Email successfully sent to Admin: " + adminEmail);

        } catch (Exception e) {
            logger.error("Failed to send order email to admin: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. SEND EMAIL TO CUSTOMER
    // ==========================================
    @Async
    public void sendOrderConfirmationToCustomer(Order order) {

        // Check if the customer actually provided an email
        if (order.getUser() == null || order.getUser().getEmail() == null
                || order.getUser().getEmail().trim().isEmpty()) {
            logger.warn("Customer did not provide an email address. Customer email skipped.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Order Confirmation - Venkat Incubators (Order #" + order.getId() + ")");

            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; padding: 20px; border-radius: 10px;'>"
                    + "<h2 style='color: #2563EB;'>Thank you for your order, "
                    + order.getUser().getFullName().split(" ")[0] + "!</h2>"
                    + "<p>We have successfully received your order and are preparing it for dispatch.</p>"
                    + "<div style='background-color: #f3f4f6; padding: 15px; border-radius: 8px; margin-top: 20px;'>"
                    + "<h3 style='margin-top: 0; color: #1f2937;'>Order Summary</h3>"
                    + "<p><strong>Order ID:</strong> #" + order.getId() + "</p>" + "<p><strong>Product:</strong> "
                    + order.getProduct().getTitle() + "</p>" + "<p><strong>Quantity:</strong> " + order.getQuantity()
                    + "</p>" + "<p><strong>Total Paid:</strong> ₹" + order.getTotalPrice() + "</p>"
                    + "<p><strong>Delivery Address:</strong> " + order.getUser().getAddress() + "</p>" + "</div>"
                    + "<p style='margin-top: 20px;'>We will notify you via SMS/Email once your order is in transit.</p>"
                    + "<p>Best Regards,<br><strong>Venkat Incubators Team</strong></p>" + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            logger.info("Order Confirmation Email successfully sent to Customer: " + order.getUser().getEmail());

        } catch (Exception e) {
            logger.error("Failed to send order email to customer: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. DYNAMIC STATUS UPDATES (Customer)
    // ==========================================
    @Async
    public void sendOrderStatusEmail(Order order) {
        if (order.getUser() == null || order.getUser().getEmail() == null
                || order.getUser().getEmail().trim().isEmpty())
            return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(order.getUser().getEmail());

            String subject = "";
            String title = "";
            String body = "";
            String highlightBox = "Order ID: #" + order.getId() + "<br>Product: " + order.getProduct().getTitle();

            switch (order.getStatus()) {
            case ACCEPTED:
                subject = "Order Accepted - Venkat Incubators";
                title = "Your order has been accepted!";
                body = "Great news! We have accepted your order and are currently packing your incubator for shipment.";
                break;
            case IN_TRANSIT:
                subject = "Order Dispatched! Action Required - Venkat Incubators";
                title = "Your order is on the way! 🚚";
                body = "Your order has been dispatched and is currently in transit. <strong>Please provide the OTP below to the delivery agent to receive your package.</strong>";
                highlightBox += "<br><br><span style='font-size: 24px; font-weight: bold; color: #D97706; letter-spacing: 5px;'>OTP: "
                        + order.getDeliveryOtp() + "</span>";
                break;
            case DELIVERED:
                subject = "Order Delivered - Venkat Incubators";
                title = "Enjoy your new incubator! 🎉";
                body = "Your order has been successfully delivered. Thank you for shopping with Venkat Incubators! We'd love to hear your feedback—please log in to leave a review.";
                break;
            case CANCELLED:
                subject = "Order Cancelled - Venkat Incubators";
                title = "Order Cancellation Notice";
                body = "We regret to inform you that your order has been cancelled. If a payment was made, it will be refunded to your original payment method within 5-7 business days. Please contact support if you have any questions.";
                break;
            default:
                return; // Don't send emails for unknown statuses
            }

            helper.setSubject(subject);
            helper.setText(buildEmailTemplate(subject, title, body, highlightBox), true);
            mailSender.send(message);
            logger.info("Status Update Email sent to: " + order.getUser().getEmail());

        } catch (Exception e) {
            logger.error("Status Email Failed: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. REVIEW THANK YOU (Customer)
    // ==========================================
    @Async
    public void sendReviewThankYouEmail(User user, Product product) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty())
            return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Thank you for your review! - Venkat Incubators");

            String htmlContent = buildEmailTemplate("Review Published", "We appreciate your feedback! 🌟",
                    "Thank you for taking the time to review the <strong>" + product.getTitle()
                            + "</strong>. Your insights help us improve and assist other buyers in making the right choice.",
                    "Your review is now live on our platform.");

            helper.setText(htmlContent, true);
            mailSender.send(message);
            logger.info("Review Thank You Email sent to: " + user.getEmail());
        } catch (Exception e) {
            logger.error("Review Email Failed: " + e.getMessage());
        }
    }

    // ==========================================
    // 5. SEND OTP EMAIL (Verification)
    // ==========================================
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your Verification Code - Venkat Incubators");

            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #e5e7eb; border-radius: 12px; padding: 30px; text-align: center;'>"
                    + "<h2 style='color: #2563eb; margin-bottom: 20px;'>Email Verification</h2>"
                    + "<p style='font-size: 16px; color: #4b5563;'>Please use the following One-Time Password (OTP) to verify your email address. This code is valid for <strong>10 minutes</strong>.</p>"
                    + "<div style='background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 25px 0;'>"
                    + "<span style='font-size: 32px; font-weight: bold; color: #111827; letter-spacing: 8px;'>" + otp
                    + "</span>" + "</div>"
                    + "<p style='font-size: 14px; color: #ef4444;'>Do not share this code with anyone.</p>" + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            logger.info("OTP Email sent to: " + toEmail);

        } catch (Exception e) {
            logger.error("OTP Email Failed: " + e.getMessage());
        }
    }

    // --- Private Helper to generate beautiful HTML ---
    private String buildEmailTemplate(String header, String title, String body, String highlightBox) {
        return "<div style='font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden;'>"
                + "<div style='background-color: #1e3a8a; padding: 20px; text-align: center; color: white;'>"
                + "<h2 style='margin: 0; font-size: 20px; tracking: 2px;'>" + header.toUpperCase() + "</h2>" + "</div>"
                + "<div style='padding: 30px;'>" + "<h3 style='color: #2563eb; font-size: 22px; margin-top: 0;'>"
                + title + "</h3>" + "<p style='font-size: 16px; line-height: 1.5; color: #4b5563;'>" + body + "</p>"
                + "<div style='background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin-top: 25px; border: 1px solid #e5e7eb;'>"
                + "<p style='margin: 0; font-size: 15px;'>" + highlightBox + "</p>" + "</div>"
                + "<p style='margin-top: 30px; font-size: 14px; color: #6b7280;'>Best Regards,<br><strong style='color: #111827;'>Venkat Incubators Team</strong></p>"
                + "</div>"
                + "<div style='background-color: #f9fafb; padding: 15px; text-align: center; border-top: 1px solid #e5e7eb; font-size: 12px; color: #9ca3af;'>"
                + "&copy; " + java.time.Year.now().getValue() + " Venkat Incubators. All rights reserved." + "</div>"
                + "</div>";
    }
}