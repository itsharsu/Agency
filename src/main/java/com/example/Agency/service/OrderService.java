package com.example.Agency.service;

import com.example.Agency.dto.ApiResponse;
import com.example.Agency.dto.OrderDetailDto;
import com.example.Agency.dto.OrderDto;
import com.example.Agency.model.OrderDetails;
import com.example.Agency.model.Orders;
import com.example.Agency.model.Product;
import com.example.Agency.repository.OrderDetailRepository;
import com.example.Agency.repository.OrderRepository;
import com.example.Agency.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {

    private OrderRepository orderRepository;
    private OrderDetailRepository orderDetailRepository;
    private ProductRepository productRepository;

    @Transactional
    public ApiResponse<Orders> createOrUpdateOrder(OrderDto orderDTO, List<OrderDetailDto> orderDetailDTOs) {
        // Check the current time
        LocalTime currentTime = LocalTime.now();
        LocalDate currentDate = LocalDate.now();

        // Check order shift and enforce time restrictions
        if (orderDTO.isOrderShift()) { // True = AM shift
            if (currentTime.isAfter(LocalTime.of(23, 0))) {
                throw new IllegalArgumentException("Orders in AM shift must be placed before 23:00:00.");
            }
        } else { // False = PM shift
            if (currentTime.isAfter(LocalTime.of(8, 30))) {
                throw new IllegalArgumentException("Orders in PM shift must be placed before 8:30:00.");
            }
        }

        // Check if the retailer has already created an order today
        Optional<Orders> existingOrderOpt = orderRepository.findByUserIdAndOrderDate(orderDTO.getUserId(), currentDate);

        Orders order;
        if (existingOrderOpt.isPresent()) {
            // Order exists for today, so we'll update the existing order by adding more items
            order = existingOrderOpt.get();
        } else {
            // Create a new order (Auto increment will handle the order ID)
            order = new Orders();
            order.setOrderId(UUID.randomUUID().toString());
            order.setUserId(orderDTO.getUserId());
            order.setOrderDate(currentDate);
            order.setOrderTime(currentTime);
            order.setOrderShift(orderDTO.isOrderShift());

            // Save the new order
            order = orderRepository.save(order);  // Let JPA handle the auto-increment of the order ID
        }

        // Collect product IDs from order details
        List<String> productIds = orderDetailDTOs.stream()
                .map(OrderDetailDto::getProductId)
                .collect(Collectors.toList());

        // Fetch all products in one go
        Map<String, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));

        BigDecimal totalAmount = BigDecimal.valueOf(order.getTotalAmount() != null ? order.getTotalAmount() : 0);

        // Create and Save OrderDetails
        for (OrderDetailDto detailDTO : orderDetailDTOs) {
            Product product = productMap.get(detailDTO.getProductId());
            if (product == null) {
                throw new RuntimeException("Product with ID " + detailDTO.getProductId() + " not found");
            }

            BigDecimal price = BigDecimal.valueOf(product.getUnitPrice());
            BigDecimal quantity = BigDecimal.valueOf(detailDTO.getQuantity());
            BigDecimal subtotal = price.multiply(quantity);

            // Check if the order detail for the product already exists
            Optional<OrderDetails> existingOrderDetail = orderDetailRepository.findByOrderIdAndProductId(order.getOrderId(), detailDTO.getProductId());

            OrderDetails orderDetail;
            if (existingOrderDetail.isPresent()) {
                // Update the existing order detail
                orderDetail = existingOrderDetail.get();
                orderDetail.setQuantity(orderDetail.getQuantity() + detailDTO.getQuantity());
            } else {
                // Create new order detail
                orderDetail = new OrderDetails();
                orderDetail.setOrder_detail_Id(UUID.randomUUID().toString());
                orderDetail.setOrderId(order.getOrderId());
                orderDetail.setProductId(detailDTO.getProductId());
                orderDetail.setQuantity(detailDTO.getQuantity());
                orderDetail.setPrice(price.doubleValue());
            }

            // Save the order detail
            orderDetailRepository.save(orderDetail);

            // Update total amount
            totalAmount = totalAmount.add(subtotal);
        }

        // Update and save the order with the new total amount
        order.setTotalAmount(totalAmount.doubleValue());
        Orders createdOrder = orderRepository.save(order);

        return new ApiResponse<>(true,"Order created!",createdOrder,null);
    }

}
