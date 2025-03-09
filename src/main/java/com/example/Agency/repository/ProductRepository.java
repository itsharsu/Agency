package com.example.Agency.repository;

import com.example.Agency.domain.ProductStatus;
import com.example.Agency.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,String> {
    List<Product>  findByStatus(ProductStatus status);

//    @Query(value = "SELECT productId, productName FROM Product WHERE status = 'Available'", nativeQuery = true)
//    List<Object[]> getAllAvailableProducts();
}
