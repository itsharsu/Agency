package com.example.Agency.dto;


import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private OrderDto order;
    private List<OrderDetailDto> orderDetails;
}