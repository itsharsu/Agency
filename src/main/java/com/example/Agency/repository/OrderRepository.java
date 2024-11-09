package com.example.Agency.repository;

import com.example.Agency.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, String> {

    @Query(value = "SELECT o.order_id AS orderId, " +
            "u.user_id AS userId, " +
            "u.shop_name AS shopName, " +
            "DATE_FORMAT(o.order_date, '%Y-%m-%d') AS orderDate, " +
            "DATE_FORMAT(o.order_time, '%H:%i:%s') AS orderTime, " +
            "p.product_name AS productName, " +
            "od.quantity AS quantity, " +
            "od.subtotal AS subtotal, " +
            "od.cost_subtotal AS costSubtotal, " +  // Added comma here
            "SUM(od.subtotal) OVER (PARTITION BY o.order_id) AS totalAmount, " +  // Added comma here
            "SUM(od.cost_subtotal) OVER (PARTITION BY o.order_id) AS costAmount " +
            "FROM orders o " +
            "JOIN user u ON o.user_id = u.user_id " +
            "JOIN order_details od ON o.order_id = od.order_id " +
            "JOIN product p ON od.product_id = p.product_id " +
            "ORDER BY o.order_id", nativeQuery = true)
    List<Object[]> findAllOrdersWithProductInfoNative();

    @Query("SELECT o FROM Orders o WHERE o.userId = :userId AND o.orderDate = :orderDate AND o.orderShift = :orderShift")
    Optional<Orders> findByUserIdAndOrderDateAndOrderShift(String userId, LocalDate orderDate, boolean orderShift);

    @Query(value = "SELECT o.order_id AS orderId, " +
            "u.user_id AS userId, " +
            "u.shop_name AS shopName, " +
            "DATE_FORMAT(o.order_date, '%Y-%m-%d') AS orderDate, " +
            "DATE_FORMAT(o.order_time, '%H:%i:%s') AS orderTime, " +
            "p.product_name AS productName, " +
            "od.quantity AS quantity, " +
            "od.subtotal AS subtotal, " +
            "od.cost_subtotal AS costSubtotal, " +  // Added comma here
            "SUM(od.subtotal) OVER (PARTITION BY o.order_id) AS totalAmount, " +  // Added comma here
            "SUM(od.cost_subtotal) OVER (PARTITION BY o.order_id) AS costAmount " +
            "FROM orders o " +
            "JOIN user u ON o.user_id = u.user_id " +
            "JOIN order_details od ON o.order_id = od.order_id " +
            "JOIN product p ON od.product_id = p.product_id " +
            "WHERE u.user_id = :userId " +
            "ORDER BY o.order_id", nativeQuery = true)
    List<Object[]> findOrdersByUserId(@Param("userId") String userId);

    @Query(value = "SELECT o.order_id AS orderId, " +
            "u.user_id AS userId, " +
            "u.shop_name AS shopName, " +
            "DATE_FORMAT(o.order_date, '%Y-%m-%d') AS orderDate, " +
            "DATE_FORMAT(o.order_time, '%H:%i:%s') AS orderTime, " +
            "p.product_name AS productName, " +
            "od.quantity AS quantity, " +
            "od.subtotal AS subtotal, " +
            "od.cost_subtotal AS costSubtotal, " +  // Added comma here
            "SUM(od.subtotal) OVER (PARTITION BY o.order_id) AS totalAmount, " +  // Added comma here
            "SUM(od.cost_subtotal) OVER (PARTITION BY o.order_id) AS costAmount " +
            "FROM orders o " +
            "JOIN user u ON o.user_id = u.user_id " +
            "JOIN order_details od ON o.order_id = od.order_id " +
            "JOIN product p ON od.product_id = p.product_id " +
            "WHERE (:orderDate IS NULL OR o.order_date = :orderDate) " +
            "AND (:shift IS NULL OR o.order_shift = :shift) " +
            "AND (:startDate IS NULL OR o.order_date >= :startDate) " +
            "AND (:endDate IS NULL OR o.order_date <= :endDate) " +
            "ORDER BY o.order_id", nativeQuery = true)
    List<Object[]> findAllOrdersWithProductInfoByDateRangeOrShift(
            @Param("orderDate") String orderDate,
            @Param("shift") Boolean shift,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

}
