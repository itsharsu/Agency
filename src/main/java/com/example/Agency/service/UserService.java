package com.example.Agency.service;

import com.example.Agency.dto.ApiResponse;
import com.example.Agency.model.User;
import com.example.Agency.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        log.info("Retrieved {} users", users.size());
        return new ApiResponse<>(true, "Users retrieved successfully", users, null);
    }

    public ApiResponse<Optional<User>> getUserById(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            log.info("User found with id: {}", userId);
            return new ApiResponse<>(true, "User found!", user, null);
        } else {
            log.warn("User not found with id: {}", userId);
            return new ApiResponse<>(false, "User not Found", null, null);
        }
    }

    @Transactional
    public ApiResponse<String> deleteUser(String userID) {
        if (userRepository.existsById(userID)) {
            userRepository.deleteById(userID); // Delete the user
            log.info("Successfully deleted user with id: {}", userID);
            return new ApiResponse<>(true, "Successfully Deleted!", null, null);
        } else {
            log.warn("Unable to delete: User not found with id: {}", userID);
            return new ApiResponse<>(false, "Unable to delete: User not found", null, null);
        }
    }

    @Transactional
    public ApiResponse<User> updateUser(String userId, User updatedUser) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user fields as needed
            user.setUserName(updatedUser.getUserName());
            user.setShopName(updatedUser.getShopName());
            user.setAddress(updatedUser.getAddress());
            // You might skip updating the password field if not needed

            User savedUser = userRepository.save(user); // Save updated user to database
            log.info("Updated user with id: {}", userId);
            return new ApiResponse<>(true, "User updated successfully", savedUser, null);
        } else {
            log.warn("User not found with id: {}", userId);
            return new ApiResponse<>(false, "User not found", null, null);
        }
    }
}
