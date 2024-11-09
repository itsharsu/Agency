package com.example.Agency.dto;

import lombok.Data;

@Data
public class UserOrderInfo {
    private String orderId;
    private String userId;// Keep orderId
    private String shopName;
    private String orderDate;
    private String orderTime;
    private String productName;
    private Integer quantity;
    private Double subtotal;
    private Double costSubtotal;
}
