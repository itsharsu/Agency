package com.example.Agency.model;


import com.example.Agency.domain.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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
    @DecimalMin(value = "0.0", inclusive = true)
    private double unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true)
    private double originalPrice;

    private String productImage;


    // Constructor, getters, setters, and other necessary methods
}
