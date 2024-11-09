package com.example.Agency.service;

import com.example.Agency.dto.*;
import com.example.Agency.model.OrderDetails;
import com.example.Agency.model.Orders;
import com.example.Agency.model.Product;
import com.example.Agency.model.User;
import com.example.Agency.repository.OrderDetailRepository;
import com.example.Agency.repository.OrderRepository;
import com.example.Agency.repository.ProductRepository;
import com.example.Agency.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {

    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private OrderDetailRepository orderDetailRepository;
    private ProductRepository productRepository;


    @Transactional
    public ApiResponse<Orders> createOrUpdateOrder(OrderDto orderDTO, List<OrderDetailDto> orderDetailDTOs) {
        LocalTime currentTime = LocalTime.now();
//        LocalDate currentDate = LocalDate.now();

        // Enforce order shift time restrictions
//        if (orderDTO.isOrderShift()) { // AM shift
//            if (currentTime.isAfter(LocalTime.of(23, 0))) {
//                throw new IllegalArgumentException("Orders in AM shift must be placed before 23:00.");
//            }
//        } else { // PM shift
//            if (currentTime.isAfter(LocalTime.of(8, 30))) {
//                throw new IllegalArgumentException("Orders in PM shift must be placed before 08:30.");
//            }
//        }

        // Check if an order exists for this user on the current date and shift
        Optional<Orders> existingOrderOpt = orderRepository.findByUserIdAndOrderDateAndOrderShift(
                orderDTO.getUserId(), orderDTO.getOrderDate(), orderDTO.isOrderShift()
        );

        Orders order;
        if (existingOrderOpt.isPresent()) {
            // Update existing order
            order = existingOrderOpt.get();
        } else {
            // Create a new order if no order exists for the current date and shift
            order = new Orders();
            order.setOrderId(UUID.randomUUID().toString());
            order.setUserId(orderDTO.getUserId());
            order.setOrderDate(orderDTO.getOrderDate());
            order.setOrderTime(currentTime);
            order.setOrderShift(orderDTO.isOrderShift());
            order.setTotalAmount(0.0);
            order.setCostAmount(0.0);
            order = orderRepository.save(order); // Save initial order to the database
        }

        // Retrieve products and calculate the total order amount
        List<String> productIds = orderDetailDTOs.stream()
                .map(OrderDetailDto::getProductId)
                .collect(Collectors.toList());

        Map<String, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));

        BigDecimal totalAmount = BigDecimal.valueOf(order.getTotalAmount() != null ? order.getTotalAmount() : 0);
        //change
        BigDecimal costAmount = BigDecimal.valueOf(order.getCostAmount() != null ? order.getCostAmount() : 0);

        // Retrieve the user to update their due amount
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User with ID " + orderDTO.getUserId() + " not found"));
        BigDecimal userDueAmount = BigDecimal.valueOf(user.getDueAmount() != null ? user.getDueAmount() : 0);

        for (OrderDetailDto detailDTO : orderDetailDTOs) {
            Product product = productMap.get(detailDTO.getProductId());
            if (product == null) {
                throw new RuntimeException("Product with ID " + detailDTO.getProductId() + " not found");
            }

            BigDecimal cost = BigDecimal.valueOf(product.getOriginalPrice());
            BigDecimal price = BigDecimal.valueOf(product.getUnitPrice());
            BigDecimal quantity = BigDecimal.valueOf(detailDTO.getQuantity());
            BigDecimal subtotal = price.multiply(quantity);
            BigDecimal costSubtotal = cost.multiply(quantity);


            Optional<OrderDetails> existingOrderDetail = orderDetailRepository.findByOrderIdAndProductId(order.getOrderId(), detailDTO.getProductId());

            OrderDetails orderDetail;
            if (existingOrderDetail.isPresent()) {
                // Update existing order detail
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
                orderDetail.setCost(cost.doubleValue());
            }

            // Save each order detail
            orderDetailRepository.save(orderDetail);

            // Update user's due amount
            userDueAmount = userDueAmount.add(subtotal);
            totalAmount = totalAmount.add(subtotal);
            costAmount = costAmount.add(costSubtotal);
        }

        // Update total amount for the order
        order.setTotalAmount(totalAmount.doubleValue());
        order.setCostAmount(costAmount.doubleValue());
        Orders createdOrUpdatedOrder = orderRepository.save(order);

        // Update and save user's due amount
        user.setDueAmount(userDueAmount.doubleValue());
        userRepository.save(user);

        // Return ApiResponse with the calculated estimated profit
        return new ApiResponse<>(true, "Order created or updated successfully!", createdOrUpdatedOrder, null);
    }

    public ApiResponse<GetOrdersDto> getAllOrders() {
        List<Object[]> results = orderRepository.findAllOrdersWithProductInfoNative();

        List<UserOrderDto> orders = new ArrayList<>();
        Map<String, UserOrderDto> orderMap = new HashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;// Initialize grand total as BigDecimal
        BigDecimal costGrandTotal = BigDecimal.ZERO;


        for (Object[] result : results) {
            String orderId = (String) result[0];
            String userId = (String) result[1];
            String shopName = (String) result[2];
            String orderDate = (String) result[3];
            String orderTime = (String) result[4];
            String productName = (String) result[5];
            int quantity = ((Number) result[6]).intValue();

            // Use BigDecimal for subtotal and totalAmount
            BigDecimal subtotal = (BigDecimal) result[7];
            BigDecimal costSubtotal = (BigDecimal) result[8];
            BigDecimal totalAmount = (BigDecimal) result[9];
            BigDecimal costAmount = (BigDecimal) result[10];

            UserOrderDto order = orderMap.get(orderId);
            if (order == null) {
                // If the order is new, add it to the map and list
                order = new UserOrderDto(orderId, userId, shopName, orderDate, orderTime, new ArrayList<>(), totalAmount, costAmount);
                orderMap.put(orderId, order);
                orders.add(order);

                // Add totalAmount only the first time the order is processed
                grandTotal = grandTotal.add(totalAmount);
            }

            // Add product details to the order
            order.getProducts().add(new UserOrderDto.ProductInfoDto(productName, quantity, subtotal, costSubtotal));
        }

        // Convert grandTotal to double if necessary
        double grandTotalDouble = grandTotal.doubleValue();
        double costGrandTotalDouble = costGrandTotal.doubleValue();


        // Create the DTO containing orders and grand total
        GetOrdersDto responseDto = new GetOrdersDto(orders, grandTotalDouble, costGrandTotalDouble);

        // Return as ApiResponse with the custom DTO
        return new ApiResponse<>(true, "Orders retrieved successfully!", responseDto, null);
    }


    public ApiResponse<OrderResponseDto> getOrdersByRetailerId(String userId) {
        List<Object[]> orderData = orderRepository.findOrdersByUserId(userId);

        List<UserOrderInfo> userOrderInfo = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO; // Initialize totalAmount as BigDecimal
        BigDecimal costAmount = BigDecimal.ZERO;

        for (Object[] obj : orderData) {
            UserOrderInfo dto = getUserOrderInfo(obj);

            // Get the total amount only once
            totalAmount = (BigDecimal) obj[9]; // totalAmount should be BigDecimal
            costAmount = (BigDecimal) obj[10];

            userOrderInfo.add(dto);
        }
        OrderResponseDto orderResponseDto = new OrderResponseDto(userOrderInfo, totalAmount.doubleValue(), costAmount.doubleValue());
        // Convert totalAmount to double before returning if necessary
        return new ApiResponse<>(true, "Orders retrieved successfully!", orderResponseDto, null);
    }

    private static UserOrderInfo getUserOrderInfo(Object[] obj) {
        UserOrderInfo dto = new UserOrderInfo();
        dto.setOrderId((String) obj[0]);
        dto.setUserId((String) obj[1]);
        dto.setShopName((String) obj[2]);
        dto.setOrderDate((String) obj[3]);
        dto.setOrderTime((String) obj[4]);
        dto.setProductName((String) obj[5]);
        dto.setQuantity((Integer) obj[6]);

        // Use BigDecimal for subtotal
        BigDecimal subtotal = (BigDecimal) obj[7];
        BigDecimal costSubtotal = (BigDecimal) obj[8];
        dto.setSubtotal(subtotal.doubleValue());// Convert to double for setting in DTO
        dto.setCostSubtotal(costSubtotal.doubleValue());
        return dto;
    }


    public ApiResponse<GetOrdersDto> getAllOrdersByDateRangeOrShift(String orderDate, Boolean shift, String startDate, String endDate) {
        List<Object[]> results = orderRepository.findAllOrdersWithProductInfoByDateRangeOrShift(orderDate, shift, startDate, endDate);

        List<UserOrderDto> orders = new ArrayList<>();
        Map<String, UserOrderDto> orderMap = new HashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO; // Initialize grand total
        BigDecimal costGrandTotal = BigDecimal.ZERO;

        for (Object[] result : results) {
            String orderId = (String) result[0];
            String userId = (String) result[1];
            String shopName = (String) result[2];
            String orderDateResult = (String) result[3];
            String orderTime = (String) result[4];
            String productName = (String) result[5];
            int quantity = ((Number) result[6]).intValue();

            // Use BigDecimal for subtotal and totalAmount
            BigDecimal subtotal = (BigDecimal) result[7];
            BigDecimal costSubtotal = (BigDecimal) result[8];
            BigDecimal totalAmount = (BigDecimal) result[9];
            BigDecimal costAmount = (BigDecimal) result[10];

            UserOrderDto order = orderMap.get(orderId);
            if (order == null) {
                // If the order is new, add it to the map and list
                order = new UserOrderDto(orderId, userId, shopName, orderDateResult, orderTime, new ArrayList<>(), totalAmount, costAmount);
                orderMap.put(orderId, order);
                orders.add(order);

                // Add totalAmount only the first time the order is processed
                grandTotal = grandTotal.add(totalAmount);
                costGrandTotal = costGrandTotal.add(costAmount);
            }

            // Add product details to the order
            order.getProducts().add(new UserOrderDto.ProductInfoDto(productName, quantity, subtotal, costSubtotal));
        }

        // Convert grandTotal to double if necessary
        double grandTotalDouble = grandTotal.doubleValue();
        double costGrantTotalDouble = costGrandTotal.doubleValue();

        // Create the DTO containing orders and grand total
        GetOrdersDto responseDto = new GetOrdersDto(orders, grandTotalDouble, costGrantTotalDouble);

        // Return as ApiResponse with the custom DTO
        return new ApiResponse<>(true, "Orders retrieved successfully!", responseDto, null);
    }

}
