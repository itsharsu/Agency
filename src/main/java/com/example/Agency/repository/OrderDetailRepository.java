package com.example.Agency.repository;

import com.example.Agency.model.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<OrderDetails,String> {
    Optional<OrderDetails> findByOrderIdAndProductId(String orderId, String productId);
}
