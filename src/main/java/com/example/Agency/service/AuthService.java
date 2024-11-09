package com.example.Agency.service;

import com.example.Agency.dto.ApiResponse;
import com.example.Agency.model.User;
import com.example.Agency.repository.UserRepository;
import com.example.Agency.security.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@AllArgsConstructor
public class AuthService {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private JwtUtil jwtUtil;


    public ApiResponse<String> createUser(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash())); // Hash the password
        User createdUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(createdUser.getMobileNumber());

        return new ApiResponse<>(true,"User Created",token,null);
    }

    public ApiResponse<String> login(String mobileNumber, String password) {
        Optional<User> user = userRepository.findByMobileNumber(mobileNumber);
        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPasswordHash())) {
            return new ApiResponse<>(false,"Invalid mobile number or password", null,null);
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.get().getMobileNumber());

        return new ApiResponse<>(true,"Login successful", token,null);
    }

}
