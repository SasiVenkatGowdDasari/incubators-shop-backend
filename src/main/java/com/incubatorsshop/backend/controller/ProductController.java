package com.incubatorsshop.backend.controller;

import com.incubatorsshop.backend.entity.Order;
import com.incubatorsshop.backend.entity.OrderStatus;
import com.incubatorsshop.backend.entity.Product;
import com.incubatorsshop.backend.entity.Review;
import com.incubatorsshop.backend.repository.OrderRepository;
import com.incubatorsshop.backend.repository.ProductRepository;
import com.incubatorsshop.backend.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
//@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    public ProductController(ProductRepository productRepository, ReviewRepository reviewRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
    }

    // ==========================================
    // GET ENDPOINTS
    // ==========================================
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        // Fetch ALL products (both active and inactive) for the admin inventory page.
        // We handle filtering active products vs inactive products on the frontend.
        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            calculateDynamicStats(p);
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id).map(p -> {
            calculateDynamicStats(p);
            return ResponseEntity.ok(p);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // POST / PUT ENDPOINTS (ADD / EDIT)
    // ==========================================
    
    @PostMapping
    public ResponseEntity<?> addProduct(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("actualPrice") Double actualPrice,
            @RequestParam("currentPrice") Double currentPrice,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("material") String material,
            @RequestParam("type") String type,
            @RequestParam("capacity") String capacity,
            @RequestParam("warranty") String warranty,
            @RequestParam("shippingOptions") String shippingOptions,
            @RequestParam(value = "active", defaultValue = "true") Boolean active, // Accepts the visibility toggle
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "videos", required = false) List<MultipartFile> videos) {

        try {
            Product product = new Product();
            updateProductDetails(product, title, description, actualPrice, currentPrice, stockQuantity, material, type, capacity, warranty, shippingOptions, active, images, videos, "", "");
            return ResponseEntity.ok(productRepository.save(product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editProduct(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("actualPrice") Double actualPrice,
            @RequestParam("currentPrice") Double currentPrice,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("material") String material,
            @RequestParam("type") String type,
            @RequestParam("capacity") String capacity,
            @RequestParam("warranty") String warranty,
            @RequestParam("shippingOptions") String shippingOptions,
            @RequestParam(value = "active", defaultValue = "true") Boolean active, // Accepts the visibility toggle
            @RequestParam(value = "existingImageUrls", defaultValue = "") String existingImageUrls,
            @RequestParam(value = "existingVideoUrls", defaultValue = "") String existingVideoUrls,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "videos", required = false) List<MultipartFile> videos) {

        try {
            Optional<Product> optionalProduct = productRepository.findById(id);
            if (optionalProduct.isEmpty()) return ResponseEntity.notFound().build();
            
            Product product = optionalProduct.get();
            updateProductDetails(product, title, description, actualPrice, currentPrice, stockQuantity, material, type, capacity, warranty, shippingOptions, active, images, videos, existingImageUrls, existingVideoUrls);
            return ResponseEntity.ok(productRepository.save(product));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed: " + e.getMessage());
        }
    }

    // ==========================================
    // DELETE ENDPOINT (SOFT DELETE)
    // ==========================================
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return productRepository.findById(id).map(product -> {
            
            // Soft Delete: Hide from catalog instead of running SQL DELETE to protect existing orders
            product.setActive(false);
            productRepository.save(product);
            
            return ResponseEntity.ok().body("Product removed from catalog successfully");
            
        }).orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConflict(org.springframework.dao.DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Cannot delete this product because it is tied to an existing customer order.");
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================
    
    private void calculateDynamicStats(Product p) {
        // 1. Calculate Average Rating
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(p.getId());
        if (reviews != null && !reviews.isEmpty()) {
            double avg = reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
            p.setRating(Math.round(avg * 10.0) / 10.0);
        } else {
            p.setRating(0.0);
        }

        // 2. Calculate Total Purchases (Only counting DELIVERED goods)
        List<Order> orders = orderRepository.findByProductId(p.getId());
        if (orders != null && !orders.isEmpty()) {
            int sales = orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                    .mapToInt(Order::getQuantity)
                    .sum();
            p.setTotalPurchases(sales);
        } else {
            p.setTotalPurchases(0);
        }
    }

    private void updateProductDetails(Product product, String title, String description, Double actualPrice, Double currentPrice, Integer sq, String mat, String type, String cap, String war, String ship, Boolean active, List<MultipartFile> imgs, List<MultipartFile> vids, String existingImages, String existingVideos) throws IOException {
        product.setTitle(title);
        product.setDescription(description);
        product.setActualPrice(actualPrice);
        product.setCurrentPrice(currentPrice);
        product.setStockQuantity(sq);
        product.setMaterial(mat);
        product.setType(type);
        product.setCapacity(cap);
        product.setWarranty(war);
        product.setShippingOptions(ship);
        
        if (active != null) {
            product.setActive(active);
        }

        String finalImages = (existingImages != null && !existingImages.isEmpty()) ? existingImages : "";
        if (imgs != null && !imgs.isEmpty()) {
            String newUrls = saveMediaFiles(imgs, "uploads/images/");
            finalImages = finalImages.isEmpty() ? newUrls : finalImages + "," + newUrls;
        }
        product.setImageUrl(finalImages.isEmpty() ? null : finalImages);

        String finalVideos = (existingVideos != null && !existingVideos.isEmpty()) ? existingVideos : "";
        if (vids != null && !vids.isEmpty()) {
            String newUrls = saveMediaFiles(vids, "uploads/videos/");
            finalVideos = finalVideos.isEmpty() ? newUrls : finalVideos + "," + newUrls;
        }
        product.setVideoUrl(finalVideos.isEmpty() ? null : finalVideos);
    }

    private String saveMediaFiles(List<MultipartFile> files, String directory) throws IOException {
        List<String> filePaths = new ArrayList<>();
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                Path path = Paths.get(directory + fileName);
                Files.write(path, file.getBytes());
                filePaths.add("/" + directory + fileName);
            }
        }
        return String.join(",", filePaths);
    }
}