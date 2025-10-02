package com.example.Agency.controller;

import com.example.Agency.domain.ProductStatus;
import com.example.Agency.dto.ApiResponse;
import com.example.Agency.model.Product;
import com.example.Agency.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> saveProduct(
            @RequestParam("productName") String productName,
            @RequestParam("unitPrice") double unitPrice,
            @RequestParam("originalPrice") double originalPrice,
            @RequestParam("status") ProductStatus status,
            @RequestParam("productImage") MultipartFile file) {

        ApiResponse<?> response = productService.saveProduct(productName, unitPrice, originalPrice, status, file);
        return ResponseEntity.ok(response);
    }



    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable("productId") String productId,
            @RequestParam String productName,
            @RequestParam double unitPrice,
            @RequestParam double originalPrice,
            @RequestParam ProductStatus status,
            @RequestParam(required = false) MultipartFile productImage) {

        ApiResponse<Product> response = productService.updateProduct(productId, productName, unitPrice, originalPrice, status, productImage);
        return ResponseEntity.ok(response);
    }


    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllProducts(){
        ApiResponse<?> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<?>> getAvailableProducts(){
        ApiResponse<?> response = productService.getAvailableProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unavailable")
    public ResponseEntity<ApiResponse<?>> getUnavailableProducts(){
        ApiResponse<?> response = productService.getUnavailableProducts();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")  // Restrict to ADMIN role
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable String productId) {
        ApiResponse<String> response = productService.deleteByProductId(productId);
        return response.isSuccess()
                ? ResponseEntity.ok(response) // 200 OK
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 Not Found
    }
}
