package com.example.Agency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportDto {

    private String orderDate;
    private String shift;
    private String productName;
    private Long quantity;
    private Double totalAmount;
    private Double totalCostAmount;
}
