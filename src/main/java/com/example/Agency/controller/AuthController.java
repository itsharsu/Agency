package com.example.Agency.controller;

import com.example.Agency.dto.ApiResponse;
import com.example.Agency.dto.reuests.LoginRequest;
import com.example.Agency.model.User;
import com.example.Agency.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
@AllArgsConstructor
public class AuthController {

    private AuthService authService;

    @GetMapping
    public ResponseEntity<String> sayHello(){
        return ResponseEntity.ok("Hello");
    }


    @PostMapping("auth/register")
    public ResponseEntity<ApiResponse<String>> RegisterUser(@Valid @RequestBody User user) {
        ApiResponse<String> response = authService.createUser(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("auth/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest loginRequest) {
        ApiResponse<String> response = authService.login(loginRequest.getMobileNumber(), loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }

}
