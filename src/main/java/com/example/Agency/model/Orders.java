package com.example.Agency.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orders{
    @Id
    private String orderId;

    private String userId;
    private LocalDate orderDate;
    private LocalTime orderTime;
    private Double totalAmount;
    private Boolean orderShift;
}
