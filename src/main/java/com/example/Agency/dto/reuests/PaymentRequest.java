package com.example.Agency.dto.reuests;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentRequest {
    private String userId;

//    @Positive(message = "Amount must be positive")
    private double amountPaid;

    private LocalDate paymentDate;

    private String receivedBy;

}
