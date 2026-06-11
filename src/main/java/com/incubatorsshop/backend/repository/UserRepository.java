package com.incubatorsshop.backend.repository;

import com.incubatorsshop.backend.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring automatically writes the SQL to find a user by their mobile number (used for Login/OTP)
    User findByMobileNumber(String mobileNumber);
    
    // Spring automatically writes the SQL to find a user by email (used for Google Auth)
    User findByEmail(String email);
    Optional<User> findFirstByRole(String role);
}