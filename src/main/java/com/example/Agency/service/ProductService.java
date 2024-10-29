package com.example.Agency.service;

import com.example.Agency.domain.ProductStatus;
import com.example.Agency.dto.ApiResponse;
import com.example.Agency.model.Product;
import com.example.Agency.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service

public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    private final String IMAGE_DIR = "D:\\Agency Project\\Agency\\Agency\\src\\main\\resources\\images\\";

    public ApiResponse<Product> saveProduct(String productName, double unitPrice, double originalPrice, ProductStatus status, MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();  // Create the directory if it does not exist
        }

        // Define the complete path where the image will be stored
        java.nio.file.Path path = Paths.get(IMAGE_DIR + fileName);
        try {
            // Save the uploaded file to the defined path
            file.transferTo(path);

            // Create a new Product object
            Product product = new Product();
            product.setProductId(UUID.randomUUID().toString());
            product.setProductName(productName);
            product.setUnitPrice(unitPrice);
            product.setOriginalPrice(originalPrice);
            product.setStatus(status);
            product.setProductImage(path.toString());  // Save the file path

            // Save the product in the database
            Product savedProduct = productRepository.save(product);

            return new ApiResponse<>(true,"Product saved successfully!", savedProduct,null);
        }catch (IOException e) {
            return new ApiResponse<>(false,"Failed to save product image!", null,e.getMessage());
        }
    }

    public ApiResponse<Product> updateProduct(String productId, String productName, double unitPrice, double originalPrice, ProductStatus status, MultipartFile productImage) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));


        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();  // Create the directory if it does not exist
        }

        existingProduct.setProductName(productName);
        existingProduct.setUnitPrice(unitPrice);
        existingProduct.setOriginalPrice(originalPrice);
        existingProduct.setStatus(status);

        if (productImage != null && !productImage.isEmpty()) {
            // Save the new image
            try {
                String imagePath = IMAGE_DIR + productImage.getOriginalFilename();
                productImage.transferTo(new File(imagePath));
                existingProduct.setProductImage(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save image: " + e.getMessage());
            }
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return new ApiResponse<>(true, "Product updated successfully", updatedProduct,null);
    }

    public ApiResponse<List<Product>> getAllProducts(){
        List<Product> products = productRepository.findAll();
        return new ApiResponse<>(true,"products are retrived succesfully!",products,null);
    }

    public ApiResponse<?> getAvailableProducts(){
        List<Product> products = productRepository.findByStatus(ProductStatus.AVAILABLE);
        if(products.isEmpty()){
            return new ApiResponse<>(true,"List is empty",products,null);
        }
        return new ApiResponse<>(true,"List of available products",products,null);
    }

    public ApiResponse<?> getUnavailableProducts(){
        List<Product> products = productRepository.findByStatus(ProductStatus.UNAVAILABLE);
        if(products.isEmpty()){
            return new ApiResponse<>(true,"List is empty",products,null);
        }
        return new ApiResponse<>(true,"List of Unavailable products",products,null);
    }

    public ApiResponse<String> deleteByProductId(String productId){
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId); // Delete the user
            return new ApiResponse<>(true, "Successfully Deleted!", null, null);
        } else {
            return new ApiResponse<>(false, "Unable to delete: Product not found", null, null);
        }
    }
}
