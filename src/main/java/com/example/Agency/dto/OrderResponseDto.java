package com.example.Agency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private List<UserOrderInfo> orderDetails;
    private Double totalAmount;
    private Double costAmount;
}
