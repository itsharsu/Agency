package com.example.Agency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopReport {
    private String shopName;
    private BigDecimal totalAmount = BigDecimal.ZERO;  // Total amount for the shop (sum of all product amounts)
    private BigDecimal dueAmount = BigDecimal.ZERO;    // Due amount (from the user table)
    private BigDecimal totalCost = BigDecimal.ZERO;    // Total cost (sum of all product cost subtotals)
    private Map<String, BigDecimal> productQuantities = new HashMap<>();  // Map of product quantities
    private Map<String, BigDecimal> productTotals = new HashMap<>();   // Map of total amounts for each product
    private Map<String, BigDecimal> productCosts = new HashMap<>();

    // Method to add product data to the report
    public void addProductData(String productName, BigDecimal quantity, BigDecimal totalAmount, BigDecimal totalCost) {
        // Update quantities and totals for this product
        productQuantities.put(productName, productQuantities.getOrDefault(productName, BigDecimal.ZERO).add(quantity));
        productTotals.put(productName, productTotals.getOrDefault(productName, BigDecimal.ZERO).add(totalAmount));
        productCosts.put(productName, productCosts.getOrDefault(productName, BigDecimal.ZERO).add(totalCost));

        // Update overall totals for the shop
        this.totalAmount = this.totalAmount.add(totalAmount);
        this.totalCost = this.totalCost.add(totalCost);
    }
}

