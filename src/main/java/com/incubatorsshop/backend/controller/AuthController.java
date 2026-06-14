package com.incubatorsshop.backend.controller;

import com.incubatorsshop.backend.dto.LoginRequest;
import com.incubatorsshop.backend.dto.RegistrationRequest;
import com.incubatorsshop.backend.dto.ResetPasswordRequest;
import com.incubatorsshop.backend.entity.User;
import com.incubatorsshop.backend.repository.UserRepository;
import com.incubatorsshop.backend.service.UserService;
import com.incubatorsshop.backend.service.OtpService;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final OtpService otpService; 

    public AuthController(UserService userService, UserRepository userRepository, OtpService otpService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        try {
            User savedUser = userService.registerDirectly(request);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByMobileNumber(request.getMobileNumber());
        if (user == null || user.getPassword() == null || !user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/check-mobile")
    public ResponseEntity<?> checkMobileExists(@RequestBody Map<String, String> request) {
        String mobileNumber = request.get("mobileNumber");
        User user = userRepository.findByMobileNumber(mobileNumber);
        
        if (user != null) {
            return ResponseEntity.ok("Account exists.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found with this number.");
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        User user = userRepository.findByMobileNumber(request.getMobileNumber());
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        user.setPassword(request.getNewPassword()); 
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully.");
    }

    @PostMapping("/send-mobile-otp")
    public ResponseEntity<?> sendMobileOtp(@RequestBody Map<String, String> payload) {
        String mobileNumber = payload.get("mobileNumber");
        if (mobileNumber == null || mobileNumber.trim().length() != 10) {
            return ResponseEntity.badRequest().body("Error: Valid 10-digit mobile number required.");
        }
        
        otpService.generateAndSendMobileOtp(mobileNumber.trim());
        return ResponseEntity.ok("Secure authentication token dispatched to mobile operator networks.");
    }

    @PostMapping("/verify-mobile-otp")
    public ResponseEntity<?> verifyMobileOtp(@RequestBody Map<String, String> payload) {
        String mobileNumber = payload.get("mobileNumber");
        String otp = payload.get("otp");
        
        boolean isValid = otpService.verifyMobileOtp(mobileNumber, otp);
        if (isValid) {
            return ResponseEntity.ok("Mobile contact channel verified successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or Expired verification code. Please request a new one.");
        }
    }
}