package com.example.Agency.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetails {
    @Id
    private String order_detail_Id = UUID.randomUUID().toString();

    // Relationship to Order
    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "orderId")
    private Orders order;

    // Relationship to Product
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "productId")
    private Product product;

    @Column(nullable = false)
    private int quantity;

    private Double price;
    private Double cost;
    private Double subtotal;
    private Double costSubtotal;
}












//
//@Entity
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class OrderDetails {
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private String order_detail_Id = UUID.randomUUID().toString();
//
//    private String orderId;
//    private String productId;
//    private int quantity;
//    private Double price;
//
//   //changes
//    private Double cost;
//
//
//    @Column(name = "subtotal")
//    private Double subtotal;
//
//    @Column(name = "cost_subtotal")
//    private Double costSubtotal;
//
//}
