package com.example.Agency.repository;

import com.example.Agency.model.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<OrderDetails,String> {
    Optional<OrderDetails> findByOrderOrderIdAndProductProductId(String orderId, String productId);

}
