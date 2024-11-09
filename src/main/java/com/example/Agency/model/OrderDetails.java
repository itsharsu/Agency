package com.example.Agency.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetails {
    @Id
    private String order_detail_Id;

    private String orderId;
    private String productId;
    private Integer quantity;
    private Double price;

   //changes
    private Double cost;
}
