package com.example.Agency.service;

import com.example.Agency.dto.ApiResponse;
import com.example.Agency.dto.response.LoginResponse;
import com.example.Agency.model.User;
import com.example.Agency.repository.UserRepository;
import com.example.Agency.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Validates that the provided mobile number is exactly 10 digits.
     *
     * @param mobileNumber the mobile number to validate
     * @throws IllegalArgumentException if the mobile number is invalid
     */
    private void validateMobileNumber(final String mobileNumber) {
        if (mobileNumber == null || !mobileNumber.matches("\\d{10}")) {
            log.warn("Invalid mobile number: {}", mobileNumber);
            throw new IllegalArgumentException("Mobile number must be exactly 10 digits");
        }
    }

    /**
     * Creates a new user, hashing the password and generating a JWT token upon successful registration.
     *
     * @param user the user to create
     * @return an ApiResponse containing a LoginResponse with the JWT token and user details
     */
    @Transactional
    public ApiResponse<LoginResponse> createUser(final User user) {
        final String mobileNumber = user.getMobileNumber();
        validateMobileNumber(mobileNumber);

        if (userRepository.findByMobileNumber(mobileNumber).isPresent()) {
            log.warn("Attempted registration with existing mobile number: {}", mobileNumber);
            throw new RuntimeException("User with mobile number " + mobileNumber + " already exists");
        }

        // Hash the password before saving
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        User createdUser;
        try {
            createdUser = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            log.error("Data integrity issue while saving user: {}", mobileNumber, ex);
            throw new RuntimeException("Failed to create user due to a data integrity issue", ex);
        }

        // Generate JWT token using the user's mobile number
        final String token = jwtUtil.generateToken(createdUser.getMobileNumber());

        // Build the response DTO that includes both the token and user details
        final LoginResponse loginResponse = new LoginResponse(token, createdUser);
        log.info("User created successfully: {}", mobileNumber);

        return new ApiResponse<>(true, "User Created", loginResponse, null);
    }

    /**
     * Authenticates a user by mobile number and password, returning a JWT token upon successful login.
     *
     * @param mobileNumber the mobile number used for login
     * @param password     the raw password
     * @return an ApiResponse containing a LoginResponse with the JWT token and user details
     */
    @Transactional(readOnly = true)
    public ApiResponse<LoginResponse> login(final String mobileNumber, final String password) {
        validateMobileNumber(mobileNumber);

        final User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> {
                    log.warn("Login attempt with invalid mobile number: {}", mobileNumber);
                    return new RuntimeException("Invalid mobile number or password");
                });

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Invalid password attempt for mobile number: {}", mobileNumber);
            throw new RuntimeException("Invalid mobile number or password");
        }

        // Generate JWT token using the user's mobile number
        final String token = jwtUtil.generateToken(user.getMobileNumber());

        final LoginResponse loginResponse = new LoginResponse(token, user);
        log.info("User logged in successfully: {}", mobileNumber);

        return new ApiResponse<>(true, "Login successful", loginResponse, null);
    }
}
