package com.example.Agency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private String userId;
    private String orderId;
    private LocalDate orderDate;
    private LocalTime orderTime;
    private Double totalAmount;
    private boolean orderShift;// true for AM, false for PM
}
