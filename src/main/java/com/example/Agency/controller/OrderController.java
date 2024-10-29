package com.example.Agency.controller;

import com.example.Agency.dto.ApiResponse;
import com.example.Agency.dto.CreateOrderRequest;
import com.example.Agency.dto.OrderDetailDto;
import com.example.Agency.dto.OrderDto;
import com.example.Agency.model.Orders;
import com.example.Agency.service.OrderService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<Orders>> createOrder(@RequestBody CreateOrderRequest request) {
        OrderDto orderDTO = request.getOrder();
        List<OrderDetailDto> orderDetails = request.getOrderDetails();

        ApiResponse<Orders> response = orderService.createOrUpdateOrder(orderDTO, orderDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
