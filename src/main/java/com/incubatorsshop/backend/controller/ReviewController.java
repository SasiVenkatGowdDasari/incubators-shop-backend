package com.incubatorsshop.backend.controller;

import com.incubatorsshop.backend.entity.Review;
import com.incubatorsshop.backend.entity.ReviewMedia;
import com.incubatorsshop.backend.repository.ReviewMediaRepository;
import com.incubatorsshop.backend.repository.ReviewRepository;
import com.incubatorsshop.backend.service.ReviewService;
import com.incubatorsshop.backend.service.FileStorageService;
import com.incubatorsshop.backend.service.EmailService; // <-- ADDED IMPORT
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final ReviewMediaRepository reviewMediaRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService; // <-- ADDED

    // <-- UPDATED CONSTRUCTOR
    public ReviewController(ReviewService rs, ReviewRepository rr, ReviewMediaRepository rmr, FileStorageService fss, EmailService emailService) {
        this.reviewService = rs;
        this.reviewRepository = rr;
        this.reviewMediaRepository = rmr;
        this.fileStorageService = fss;
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
                    String path = fileStorageService.store(file);
                    ReviewMedia media = new ReviewMedia();
                    media.setReview(review);
                    media.setFilePath(path);
                    media.setFileType(file.getContentType() != null && file.getContentType().startsWith("image") ? "IMAGE" : "VIDEO");
                    reviewMediaRepository.save(media);
                }
            }
            
            // --- NEW: Trigger Thank You Email ---
            // We run this asynchronously so the user's browser doesn't hang waiting for the email to send
            new Thread(() -> {
                try {
                    emailService.sendReviewThankYouEmail(review.getUser(), review.getProduct());
                } catch (Exception e) {
                    System.err.println("Failed to send review thank you email: " + e.getMessage());
                }
            }).start();
            
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