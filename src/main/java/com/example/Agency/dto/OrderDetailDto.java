package com.example.Agency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDto {
    private String productId;
    private Integer quantity;
    private Double price;
    private Double subtotal;
    //changes
    private Double cost;
    private Double costSubtotal;
}
