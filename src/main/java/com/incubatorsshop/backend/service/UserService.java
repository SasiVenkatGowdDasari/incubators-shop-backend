package com.incubatorsshop.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.incubatorsshop.backend.dto.RegistrationRequest;
import com.incubatorsshop.backend.dto.UserUpdateRequest;
import com.incubatorsshop.backend.entity.User;
import com.incubatorsshop.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Cloudinary cloudinary; // <-- ADDED CLOUDINARY

    // <-- UPDATED CONSTRUCTOR
    public UserService(UserRepository userRepository, Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
    }

    // --- DIRECT REGISTRATION METHOD ---
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
            // <-- REPLACED LOCAL STORAGE WITH CLOUDINARY UPLOAD
            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(profilePicture.getBytes(), 
                ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", "incubators/profiles"
                )
            );
            
            // Get the secure HTTPS URL and save it to the user profile
            String secureUrl = uploadResult.get("secure_url").toString();
            user.setProfilePicturePath(secureUrl);
        }

        return userRepository.save(user);
    }
}