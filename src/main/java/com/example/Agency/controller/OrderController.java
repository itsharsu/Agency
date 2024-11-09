package com.example.Agency.controller;

import com.example.Agency.dto.*;
import com.example.Agency.dto.reuests.CreateOrderRequest;
import com.example.Agency.model.Orders;
import com.example.Agency.service.ExcelGenerator;
import com.example.Agency.service.OrderService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private ExcelGenerator excelGenerator;

    @PostMapping
    public ResponseEntity<ApiResponse<Orders>> createOrder(@RequestBody CreateOrderRequest request) {
        OrderDto orderDTO = request.getOrder();
        List<OrderDetailDto> orderDetails = request.getOrderDetails();

        ApiResponse<Orders> response = orderService.createOrUpdateOrder(orderDTO, orderDetails);

        return status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllOrders() {
        ApiResponse<GetOrdersDto> response = orderService.getAllOrders();
        return response.isSuccess() ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<?>> getOrdersByUserId(@PathVariable("userId") String userId) {
        ApiResponse<OrderResponseDto> response = orderService.getOrdersByRetailerId(userId);
        return response.isSuccess() ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @GetMapping("/by-date-range-or-shift")
    public ApiResponse<GetOrdersDto> getAllOrdersByDateRangeOrShift(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "shift", required = false) Boolean shift,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {

        return orderService.getAllOrdersByDateRangeOrShift(date, shift, startDate, endDate);
    }

}
