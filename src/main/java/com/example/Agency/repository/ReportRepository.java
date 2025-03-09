package com.example.Agency.repository;

import com.example.Agency.dto.ReportDto;
import com.example.Agency.model.OrderDetails;
import com.example.Agency.model.Orders;
import com.example.Agency.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public interface ReportRepository extends JpaRepository<OrderDetails, UUID> {

    @Query(value = "SELECT DATE_FORMAT(o.order_date, '%Y-%m-%d') AS orderDate, " +
            "o.order_shift AS shift, " +
            "p.product_name AS productName, " +
            "SUM(od.quantity) AS quantity, " +
            "SUM(od.subtotal) AS totalAmount, " +
            "SUM(od.cost_subtotal) AS totalCostAmount " +
            "FROM orders o " +
            "JOIN order_details od ON o.order_id = od.order_id " +
            "JOIN product p ON od.product_id = p.product_id " +
            "WHERE (:orderDate IS NULL OR o.order_date = :orderDate) " +
            "AND (:shift IS NULL OR o.order_shift = :shift) " +
            "GROUP BY DATE_FORMAT(o.order_date, '%Y-%m-%d'), o.order_shift, p.product_name " +
            "ORDER BY orderDate, shift, productName", nativeQuery = true)
    List<Object[]> fetchReportData(
            @Param("orderDate") String orderDate,
            @Param("shift") Boolean shift);


}
