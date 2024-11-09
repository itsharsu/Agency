package com.example.Agency.repository;

import com.example.Agency.model.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<OrderDetails,String> {
    Optional<OrderDetails> findByOrderIdAndProductId(String orderId, String productId);


}
