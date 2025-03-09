package com.example.Agency.repository;

import com.example.Agency.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentsRepository extends JpaRepository<Payments,String>{
    List<Payments> findByUserUserId(String userId);
}
