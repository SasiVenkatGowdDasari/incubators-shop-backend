package com.incubatorsshop.backend.service;

import com.incubatorsshop.backend.dto.ReviewRequest;
import com.incubatorsshop.backend.entity.Order;
import com.incubatorsshop.backend.entity.Product;
import com.incubatorsshop.backend.entity.Review;
import com.incubatorsshop.backend.entity.User;
import com.incubatorsshop.backend.repository.OrderRepository; // ADD THIS
import com.incubatorsshop.backend.repository.ProductRepository;
import com.incubatorsshop.backend.repository.ReviewRepository;
import com.incubatorsshop.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository; // ADD THIS

    public ReviewService(ReviewRepository rr, ProductRepository pr, UserRepository ur, OrderRepository or) {
        this.reviewRepository = rr;
        this.productRepository = pr;
        this.userRepository = ur;
        this.orderRepository = or; // ADD THIS
    }

    public Review saveReview(ReviewRequest req) {
        // Fallback for old JSON requests (role null by default)
        return saveBasicReview(req.getProductId(), req.getUserId(), null, "USER", req.getRating(), req.getComment());
    }

    public Review saveBasicReview(Long productId, Long userId, Long orderId, String role, int rating, String comment) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setComment(comment);
        review.setRating(rating);
        review.setCreatedAt(LocalDateTime.now());
        reviewRepository.save(review);

        // Update the Order status to indicate it was reviewed
        if (orderId != null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                if ("ADMIN".equalsIgnoreCase(role)) {
                    order.setAdminReviewed(true);
                } else {
                    order.setUserReviewed(true);
                }
                orderRepository.save(order);
            }
        }

        return review;
    }
}