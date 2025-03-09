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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    /**
     * Creates or updates an order based on the provided order and order detail DTOs.
     * If an order exists for the given user, date, and shift, it is updated; otherwise, a new order is created.
     *
     * @param orderDTO         the order data transfer object
     * @param orderDetailDTOs  the list of order detail DTOs
     * @return an ApiResponse wrapping the created or updated order
     */
    @Transactional
    public ApiResponse<Orders> createOrUpdateOrder(OrderDto orderDTO, List<OrderDetailDto> orderDetailDTOs) {
        final LocalTime currentTime = LocalTime.now();

        // Retrieve user; throw exception if not found.
        final User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User with ID " + orderDTO.getUserId() + " not found"));

        // Attempt to find an existing order by user, date, and shift.
        final Optional<Orders> existingOrderOpt = orderRepository.findByUserUserIdAndOrderDateAndOrderShift(
                orderDTO.getUserId(), orderDTO.getOrderDate(), orderDTO.isOrderShift()
        );

        Orders order;
        if (existingOrderOpt.isPresent()) {
            order = existingOrderOpt.get();
        } else {
            // Create new order if not present.
            order = new Orders();
            order.setUser(user);
            order.setOrderDate(orderDTO.getOrderDate());
            order.setOrderTime(currentTime);
            order.setOrderShift(orderDTO.isOrderShift());
            order.setTotalAmount(0.0);
            order.setCostAmount(0.0);
            order = orderRepository.save(order); // persist initial order
        }

        // Retrieve products referenced in the order details.
        final List<String> productIds = orderDetailDTOs.stream()
                .map(OrderDetailDto::getProductId)
                .collect(Collectors.toList());

        final Map<String, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));

        // Use Optional to handle possible null totals.
        BigDecimal totalAmount = BigDecimal.valueOf(Optional.ofNullable(order.getTotalAmount()).orElse(0.0));
        BigDecimal costAmount = BigDecimal.valueOf(Optional.ofNullable(order.getCostAmount()).orElse(0.0));
        BigDecimal userDueAmount = BigDecimal.valueOf(Optional.ofNullable(user.getDueAmount()).orElse(0.0));

        // Process each order detail.
        for (OrderDetailDto detailDTO : orderDetailDTOs) {
            final Product product = productMap.get(detailDTO.getProductId());
            if (product == null) {
                throw new RuntimeException("Product with ID " + detailDTO.getProductId() + " not found");
            }

            final BigDecimal cost = BigDecimal.valueOf(product.getOriginalPrice());
            final BigDecimal price = BigDecimal.valueOf(product.getUnitPrice());
            final BigDecimal quantity = BigDecimal.valueOf(detailDTO.getQuantity());
            final BigDecimal subtotal = price.multiply(quantity);
            final BigDecimal costSubtotal = cost.multiply(quantity);

            // Check if an OrderDetail already exists for this order and product.
            final Optional<OrderDetails> existingOrderDetailOpt =
                    orderDetailRepository.findByOrderOrderIdAndProductProductId(order.getOrderId(), detailDTO.getProductId());

            OrderDetails orderDetail;
            if (existingOrderDetailOpt.isPresent()) {
                // Update the existing order detail.
                orderDetail = existingOrderDetailOpt.get();
                final int newQuantity = orderDetail.getQuantity() + detailDTO.getQuantity();
                orderDetail.setQuantity(newQuantity);
                final BigDecimal newQuantityBD = BigDecimal.valueOf(newQuantity);
                orderDetail.setSubtotal(price.multiply(newQuantityBD).doubleValue());
                orderDetail.setCostSubtotal(cost.multiply(newQuantityBD).doubleValue());
            } else {
                // Create a new order detail.
                orderDetail = new OrderDetails();
                orderDetail.setOrder(order);
                orderDetail.setProduct(product);
                orderDetail.setQuantity(detailDTO.getQuantity());
                orderDetail.setPrice(price.doubleValue());
                orderDetail.setCost(cost.doubleValue());
                orderDetail.setSubtotal(subtotal.doubleValue());
                orderDetail.setCostSubtotal(costSubtotal.doubleValue());
            }

            orderDetailRepository.save(orderDetail);

            // Update totals.
            userDueAmount = userDueAmount.add(subtotal);
            totalAmount = totalAmount.add(subtotal);
            costAmount = costAmount.add(costSubtotal);
        }

        // Update order totals and persist changes.
        order.setTotalAmount(totalAmount.doubleValue());
        order.setCostAmount(costAmount.doubleValue());
        final Orders createdOrUpdatedOrder = orderRepository.save(order);

        // Update user's due amount.
        user.setDueAmount(userDueAmount.doubleValue());
        userRepository.save(user);

        log.info("Order created/updated successfully for user ID: {}", orderDTO.getUserId());
        return new ApiResponse<>(true, "Order created or updated successfully!", createdOrUpdatedOrder, null);
    }

    /**
     * Retrieves all orders along with associated product information.
     *
     * @return an ApiResponse containing the GetOrdersDto with all orders
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public ApiResponse<GetOrdersDto> getAllOrders() {
        final List<Object[]> results = orderRepository.findAllOrdersWithProductInfoNative();
        final GetOrdersDto ordersDto = convertResultsToGetOrdersDto(results);
        return new ApiResponse<>(true, "Orders retrieved successfully!", ordersDto, null);
    }

    /**
     * Retrieves orders by retailer (user) ID.
     *
     * @param userId the retailer's user ID
     * @return an ApiResponse containing the OrderResponseDto with order information
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public ApiResponse<OrderResponseDto> getOrdersByRetailerId(String userId) {
        final List<Object[]> orderData = orderRepository.findOrdersByUserId(userId);

        final List<UserOrderInfo> userOrderInfo = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal costAmount = BigDecimal.ZERO;

        for (final Object[] obj : orderData) {
            final UserOrderInfo dto = getUserOrderInfo(obj);
            // Assuming these totals are consistent across rows; otherwise, consider accumulating.
            totalAmount = BigDecimal.valueOf(((Number) obj[9]).doubleValue());
            costAmount = BigDecimal.valueOf(((Number) obj[10]).doubleValue());
            userOrderInfo.add(dto);
        }
        final OrderResponseDto orderResponseDto = new OrderResponseDto(userOrderInfo, totalAmount.doubleValue(), costAmount.doubleValue());
        return new ApiResponse<>(true, "Orders retrieved successfully!", orderResponseDto, null);
    }

    /**
     * Retrieves orders filtered by date range or shift.
     *
     * @param orderDate the specific order date (as a String)
     * @param shift     the shift indicator
     * @param startDate the start date for filtering
     * @param endDate   the end date for filtering
     * @return an ApiResponse containing the GetOrdersDto with matching orders
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public ApiResponse<GetOrdersDto> getAllOrdersByDateRangeOrShift(String orderDate, Boolean shift, String startDate, String endDate) {
        final List<Object[]> results = orderRepository.findAllOrdersWithProductInfoByDateRangeOrShift(orderDate, shift, startDate, endDate);
        final GetOrdersDto ordersDto = convertResultsToGetOrdersDto(results);
        return new ApiResponse<>(true, "Orders retrieved successfully!", ordersDto, null);
    }

    /**
     * Converts a list of raw result arrays to a GetOrdersDto.
     *
     * @param results the list of Object arrays from the native query
     * @return a GetOrdersDto containing structured order data
     */
    private GetOrdersDto convertResultsToGetOrdersDto(final List<Object[]> results) {
        final List<UserOrderDto> orders = new ArrayList<>();
        final Map<String, UserOrderDto> orderMap = new HashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        BigDecimal costGrandTotal = BigDecimal.ZERO;

        for (final Object[] result : results) {
            final String orderId = (String) result[0];
            final String userId = (String) result[1];
            final String shopName = (String) result[2];
            final String orderDateResult = (String) result[3];
            final String orderTime = (String) result[4];
            final String productName = (String) result[5];
            final int quantity = ((Number) result[6]).intValue();

            final BigDecimal price = BigDecimal.valueOf(((Number) result[7]).doubleValue());
            final BigDecimal cost = BigDecimal.valueOf(((Number) result[8]).doubleValue());
            final BigDecimal subtotal = BigDecimal.valueOf(((Number) result[9]).doubleValue());
            final BigDecimal costSubtotal = BigDecimal.valueOf(((Number) result[10]).doubleValue());
            final BigDecimal totalAmount = BigDecimal.valueOf(((Number) result[11]).doubleValue());
            final BigDecimal costAmount = BigDecimal.valueOf(((Number) result[12]).doubleValue());

            UserOrderDto order = orderMap.get(orderId);
            if (order == null) {
                order = new UserOrderDto(orderId, userId, shopName, orderDateResult, orderTime, new ArrayList<>(), totalAmount, costAmount);
                orderMap.put(orderId, order);
                orders.add(order);

                grandTotal = grandTotal.add(totalAmount);
                costGrandTotal = costGrandTotal.add(costAmount);
            }

            order.getProducts().add(new UserOrderDto.ProductInfoDto(productName, quantity, price, cost, subtotal, costSubtotal));
        }

        return new GetOrdersDto(orders, grandTotal.doubleValue(), costGrandTotal.doubleValue());
    }

    /**
     * Helper method to extract UserOrderInfo from a raw result array.
     *
     * @param obj the raw result array
     * @return a UserOrderInfo DTO populated with data from the array
     */
    private static UserOrderInfo getUserOrderInfo(final Object[] obj) {
        final UserOrderInfo dto = new UserOrderInfo();
        dto.setOrderId((String) obj[0]);
        dto.setUserId((String) obj[1]);
        dto.setShopName((String) obj[2]);
        dto.setOrderDate((String) obj[3]);
        dto.setOrderTime((String) obj[4]);
        dto.setProductName((String) obj[5]);
        dto.setQuantity(((Number) obj[6]).intValue());

        final BigDecimal subtotal = BigDecimal.valueOf(((Number) obj[7]).doubleValue());
        final BigDecimal costSubtotal = BigDecimal.valueOf(((Number) obj[8]).doubleValue());
        dto.setSubtotal(subtotal.doubleValue());
        dto.setCostSubtotal(costSubtotal.doubleValue());
        return dto;
    }
}
