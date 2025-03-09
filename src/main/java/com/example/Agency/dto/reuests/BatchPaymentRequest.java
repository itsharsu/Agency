package com.example.Agency.dto.reuests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchPaymentRequest {
    @NotNull(message = "Payments list cannot be null")
    @Valid
    private List<PaymentRequest> payments;

}
