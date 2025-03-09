package com.example.Agency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentHistoryDto {
    private String paymentId;
    private String userId;
    private String userName;
    private String shopName;
    private LocalDate paymentDate;
    private double amountPaid;
    private String receivedBy;
}
