package com.example.Agency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderDto {
    private String orderId;
    private String userId;// Keep orderId
    private String shopName;
    private String orderDate;
    private String orderTime;
    private List<ProductInfoDto> products;
    private BigDecimal totalAmount;
    //changes
    private BigDecimal costAmount;

    @Data
    @NoArgsConstructor
    public static class ProductInfoDto {
        private String productName;
        private int quantity;
        private BigDecimal subtotal;
        private BigDecimal price;
        private BigDecimal cost;
        private BigDecimal costAmount;
        //changes
        private BigDecimal costSubtotal;


        public ProductInfoDto(String productName, int quantity, BigDecimal subtotal, BigDecimal costSubtotal) {
            this.productName = productName;
            this.quantity = quantity;
            this.subtotal = subtotal;
            this.costSubtotal = costSubtotal;
        }
    }
}
