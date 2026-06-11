package com.incubatorsshop.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    // Nullable to allow Google Auth accounts before setting a password
    @Column(nullable = true)
    private String password;

    @Column(name = "mobile_number", nullable = false, unique = true)
    private String mobileNumber;

    @Column(nullable = true)
    private String email;

    @Column(name = "profile_picture_path", nullable = true)
    private String profilePicturePath;

    @Column(nullable = false)
    private String role;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;


    @Column(name = "auth_provider", nullable = false)
    private String authProvider;

    public User() {
    }

    public User(Long id, String fullName, String address, String password, String mobileNumber, String email, String profilePicturePath, String role, boolean isVerified, String authProvider) {
        this.id = id;
        this.fullName = fullName;
        this.address = address;
        this.password = password;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.profilePicturePath = profilePicturePath;
        this.role = role;
        this.isVerified = isVerified;
        this.authProvider = authProvider;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePicturePath() { return profilePicturePath; }
    public void setProfilePicturePath(String profilePicturePath) { this.profilePicturePath = profilePicturePath; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean isVerified) { this.isVerified = isVerified; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
}