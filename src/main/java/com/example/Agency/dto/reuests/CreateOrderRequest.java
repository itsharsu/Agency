package com.example.Agency.dto.reuests;


import com.example.Agency.dto.OrderDetailDto;
import com.example.Agency.dto.OrderDto;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private OrderDto order;
    private List<OrderDetailDto> orderDetails;
}