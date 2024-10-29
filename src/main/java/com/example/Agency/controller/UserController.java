package com.example.Agency.controller;

import com.example.Agency.dto.ApiResponse;
import com.example.Agency.model.User;
import com.example.Agency.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users") // Change base URL to be more descriptive
@RequiredArgsConstructor // Use constructor injection for better testability
public class UserController {

    private final UserService userService; // Make userService final

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")  // Restrict to ADMIN role
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        ApiResponse<List<User>> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")  // Restrict to ADMIN role
    public ResponseEntity<ApiResponse<Optional<User>>> getUserById(@PathVariable String userId) {
        ApiResponse<Optional<User>> response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")  // Restrict to ADMIN role
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String userId) {
        ApiResponse<String> response = userService.deleteUser(userId);
        return response.isSuccess()
                ? ResponseEntity.ok(response) // 200 OK
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 Not Found
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable String userId,
            @RequestBody User updatedUser) {

        ApiResponse<User> response = userService.updateUser(userId, updatedUser);

        return response.isSuccess()
                ? ResponseEntity.ok(response) // 200 OK
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 Not Found
    }
}
