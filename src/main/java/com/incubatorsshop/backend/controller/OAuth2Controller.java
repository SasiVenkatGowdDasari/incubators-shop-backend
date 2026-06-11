package com.incubatorsshop.backend.controller;

import com.incubatorsshop.backend.entity.User;
import com.incubatorsshop.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
@CrossOrigin(origins = "*")
public class OAuth2Controller {

    private final UserRepository userRepository;

    public OAuth2Controller(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/google/verify")
    public ResponseEntity<?> verifyGoogleAccount(@RequestBody Map<String, String> googleData) {
        String email = googleData.get("email");
        String name = googleData.get("name");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Google Email is required for authentication.");
        }

        // Step 1: Check if this Google user already exists in our database
        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null) {
            // Step 2A: The user exists! Log them in instantly and send their profile to the frontend.
            return ResponseEntity.ok(existingUser);
        } else {
            // Step 2B: The user is NEW. 
            // We tell the React frontend they need to complete their profile (Mobile & Address).
            Map<String, String> responsePayload = new HashMap<>();
            responsePayload.put("status", "PROFILE_INCOMPLETE");
            responsePayload.put("message", "Please provide your mobile number and address to complete registration.");
            responsePayload.put("googleEmail", email);
            
            // Pass the name back so React can pre-fill the input box for them!
            if (name != null) {
                responsePayload.put("fullName", name); 
            }

            // HTTP 202 ACCEPTED means "We got your request, but more action is needed."
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responsePayload);
        }
    }
}