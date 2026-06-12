package com.incubatorsshop.backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.incubatorsshop.backend.entity.Review;
import com.incubatorsshop.backend.entity.ReviewMedia;
import com.incubatorsshop.backend.repository.ReviewMediaRepository;
import com.incubatorsshop.backend.repository.ReviewRepository;
import com.incubatorsshop.backend.service.ReviewService;
import com.incubatorsshop.backend.service.EmailService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*") // Updated for deployment
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final ReviewMediaRepository reviewMediaRepository;
    private final Cloudinary cloudinary; // <-- REPLACED FileStorageService
    private final EmailService emailService;

    public ReviewController(ReviewService rs, ReviewRepository rr, ReviewMediaRepository rmr, Cloudinary cloudinary, EmailService emailService) {
        this.reviewService = rs;
        this.reviewRepository = rr;
        this.reviewMediaRepository = rmr;
        this.cloudinary = cloudinary;
        this.emailService = emailService;
    }
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addReviewWithFiles(
            @RequestParam("productId") Long productId,
            @RequestParam("userId") Long userId,
            @RequestParam("orderId") Long orderId,
            @RequestParam("role") String role,     
            @RequestParam("rating") int rating,
            @RequestParam("comment") String comment,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        
        try {
            // Pass orderId and role to the service
            Review review = reviewService.saveBasicReview(productId, userId, orderId, role, rating, comment);

            if (files != null) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        // Upload file to Cloudinary
                        @SuppressWarnings("rawtypes")
                        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                            ObjectUtils.asMap(
                                "resource_type", "auto",
                                "folder", "incubators/reviews"
                            )
                        );

                        // Extract permanent Cloudinary URL
                        String secureUrl = uploadResult.get("secure_url").toString();

                        ReviewMedia media = new ReviewMedia();
                        media.setReview(review);
                        media.setFilePath(secureUrl); // Store URL instead of local path
                        media.setFileType(file.getContentType() != null && file.getContentType().startsWith("image") ? "IMAGE" : "VIDEO");
                        reviewMediaRepository.save(media);
                    }
                }
            }
            
            // Trigger Thank You Email (Runs instantly in the background because of @Async)
            try {
                emailService.sendReviewThankYouEmail(review.getUser(), review.getProduct());
            } catch (Exception e) {
                System.err.println("Failed to trigger review email: " + e.getMessage());
            }
            
            return ResponseEntity.ok("Review and media saved!");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }
}