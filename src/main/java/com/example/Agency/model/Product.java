package com.example.Agency.model;


import com.example.Agency.domain.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    private String productId; // UUID

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private double unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @Column(nullable = false)
    private double originalPrice;

    private String productImage;


    // Constructor, getters, setters, and other necessary methods
}
