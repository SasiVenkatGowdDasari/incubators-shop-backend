package com.incubatorsshop.backend.controller;

import com.incubatorsshop.backend.dto.UserUpdateRequest; // Ensure this is imported
import com.incubatorsshop.backend.entity.User;
import com.incubatorsshop.backend.repository.UserRepository;
import com.incubatorsshop.backend.service.UserService; // You will need to use your service
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService; // Inject UserService

    public ProfileController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        return ResponseEntity.ok(user.get());
    }

    @PutMapping(value = "/{userId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId, 
            @ModelAttribute UserUpdateRequest request,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            User updatedUser = userService.updateUserProfile(userId, request, profilePicture);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/verify-password")
    public ResponseEntity<?> verifyPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        
        User user = optionalUser.get();
        String oldPassword = request.get("password"); // Getting from JSON { "password": "..." }
        
        // If password matches DB, return OK (triggers Step 2 on frontend)
        if (user.getPassword() != null && user.getPassword().equals(oldPassword)) {
            return ResponseEntity.ok("Password verified.");
        }
        
        // If incorrect, return Unauthorized (shows "Password incorrect" error on frontend)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password.");
    }
    @PutMapping("/{userId}/password")
    public ResponseEntity<String> changePassword(@PathVariable Long userId, @RequestBody Map<String, String> passwords) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        User user = optionalUser.get();
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");

        if (user.getPassword() != null && !user.getPassword().equals(oldPassword)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect existing password.");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
        return ResponseEntity.ok("Password updated successfully.");
    }
}