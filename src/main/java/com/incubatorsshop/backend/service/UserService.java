package com.incubatorsshop.backend.service;

import com.incubatorsshop.backend.dto.RegistrationRequest;
import com.incubatorsshop.backend.dto.UserUpdateRequest;
import com.incubatorsshop.backend.entity.User;
import com.incubatorsshop.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- NEW: DIRECT REGISTRATION METHOD ---
    public User registerDirectly(RegistrationRequest request) throws Exception {
        if (userRepository.findByMobileNumber(request.getMobileNumber()) != null) {
            throw new Exception("Mobile number already exists.");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setMobileNumber(request.getMobileNumber());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); 
        user.setAddress(request.getAddress()); // This will save "Village, District, State"
        user.setRole("CUSTOMER");
        user.setVerified(true);
        user.setAuthProvider("LOCAL");

        return userRepository.save(user);
    }

    public User updateUserProfile(Long userId, UserUpdateRequest request, MultipartFile profilePicture) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setAddress(request.getAddress());

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String uploadDir = "uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs(); 
            }
            
            String fileName = System.currentTimeMillis() + "_" + profilePicture.getOriginalFilename().replaceAll("\\s+", "_");
            Path filePath = Paths.get(uploadDir, fileName);
            Files.write(filePath, profilePicture.getBytes());
            user.setProfilePicturePath(fileName);
        }

        return userRepository.save(user);
    }
}