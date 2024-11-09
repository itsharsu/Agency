package com.example.Agency.model;

import com.example.Agency.domain.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String userId = UUID.randomUUID().toString();  // UUID for the user identifier

    private String userName;
    private String shopName;
    private String address;
    private String mobileNumber;
    private String passwordHash;// Hashed password
    private Double dueAmount = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;  // Example roles: RETAILER, ADMIN

}
