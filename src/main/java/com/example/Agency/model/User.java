package com.example.Agency.model;

import com.example.Agency.domain.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String userId = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String userName;
    @Column(nullable = false)
    private String shopName;
    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String mobileNumber;

    @Column(nullable = false)
    private String passwordHash;

    @DecimalMin(value = "0.0", inclusive = true)
    private Double dueAmount = 0.0;

    @DecimalMin(value = "0.0", inclusive = true)
    private Double advance = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

}