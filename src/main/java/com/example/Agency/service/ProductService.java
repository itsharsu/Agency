package com.example.Agency.service;

import com.example.Agency.domain.ProductStatus;
import com.example.Agency.dto.ApiResponse;
import com.example.Agency.exception.FileStorageException;
import com.example.Agency.exception.ProductNotFoundException;
import com.example.Agency.model.Product;
import com.example.Agency.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    @Value("${app.image-dir}")
    private String imageDir;

    @Value("${app.image-base-url}")
    private String imageBaseUrl;

    @Transactional
    public ApiResponse<Product> saveProduct(String productName, double unitPrice, double originalPrice,
                                            ProductStatus status, MultipartFile file) {
        // Generate a unique filename and sanitize it
        String fileName = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());

        // Ensure the image directory exists
        try {
            Files.createDirectories(Paths.get(imageDir));
        } catch (IOException e) {
            logger.error("Could not create image directory: {}", e.getMessage());
            throw new FileStorageException("Could not create image directory", e);
        }

        // Define the complete path where the image will be stored
        Path path = Paths.get(imageDir, fileName);
        try {
            // Save the uploaded file to the defined path
            file.transferTo(path.toFile());
            // Construct the public URL for the stored image
            String imageUrl = imageBaseUrl + fileName;

            // Create and set up a new Product object
            Product product = new Product();
            product.setProductId(UUID.randomUUID().toString());
            product.setProductName(productName);
            product.setUnitPrice(unitPrice);
            product.setOriginalPrice(originalPrice);
            product.setStatus(status);
            product.setProductImage(imageUrl);  // Store the public URL

            // Save the product in the database
            Product savedProduct = productRepository.save(product);
            return new ApiResponse<>(true, "Product saved successfully!", savedProduct, null);
        } catch (IOException e) {
            logger.error("Failed to save product image: {}", e.getMessage());
            throw new FileStorageException("Failed to save product image", e);
        }
    }

    @Transactional
    public ApiResponse<Product> updateProduct(String productId, String productName, double unitPrice,
                                              double originalPrice, ProductStatus status,
                                              MultipartFile productImage) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // Update basic product details
        existingProduct.setProductName(productName);
        existingProduct.setUnitPrice(unitPrice);
        existingProduct.setOriginalPrice(originalPrice);
        existingProduct.setStatus(status);

        // Handle image update if a new file is provided
        if (productImage != null && !productImage.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + sanitizeFilename(productImage.getOriginalFilename());
                Path imagePath = Paths.get(imageDir, fileName);
                Files.createDirectories(imagePath.getParent());
                productImage.transferTo(imagePath.toFile());
                // Set the public URL for the stored image
                existingProduct.setProductImage(imageBaseUrl + fileName);
            } catch (IOException e) {
                logger.error("Failed to update product image for productId {}: {}", productId, e.getMessage());
                throw new FileStorageException("Failed to update product image", e);
            }
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return new ApiResponse<>(true, "Product updated successfully", updatedProduct, null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return new ApiResponse<>(true, "Products retrieved successfully!", products, null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Product>> getAvailableProducts() {
        List<Product> products = productRepository.findByStatus(ProductStatus.AVAILABLE);
        if (products.isEmpty()) {
            return new ApiResponse<>(true, "No available products found", products, null);
        }
        return new ApiResponse<>(true, "List of available products", products, null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Product>> getUnavailableProducts() {
        List<Product> products = productRepository.findByStatus(ProductStatus.UNAVAILABLE);
        if (products.isEmpty()) {
            return new ApiResponse<>(true, "No unavailable products found", products, null);
        }
        return new ApiResponse<>(true, "List of unavailable products", products, null);
    }

    @Transactional
    public ApiResponse<String> deleteByProductId(String productId) {
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
            return new ApiResponse<>(true, "Product successfully deleted!", null, null);
        } else {
            return new ApiResponse<>(false, "Unable to delete: Product not found", null, null);
        }
    }

    // Utility method to sanitize file names to avoid security issues.
    private String sanitizeFilename(String originalFilename) {
        return originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }
}
