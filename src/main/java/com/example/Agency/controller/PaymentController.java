package com.example.Agency.controller;

import com.example.Agency.dto.ApiResponse;
import com.example.Agency.dto.PaymentHistoryDto;
import com.example.Agency.dto.reuests.BatchPaymentRequest;
import com.example.Agency.dto.reuests.PaymentRequest;
import com.example.Agency.model.Payments;
import com.example.Agency.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payments>> createPayment(@Valid @RequestBody PaymentRequest request) {
            Payments payment = paymentService.createPayment(request);
            return ResponseEntity.ok(new ApiResponse<>(true,"Payment Success",payment,null));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Payments>>> createBatchPayments(
            @Valid @RequestBody BatchPaymentRequest batchRequest) {
            List<Payments> payments = paymentService.createBatchPayments(batchRequest);
            return ResponseEntity.ok(new ApiResponse<>(true,"Payment Success",payments,null));
    }

//    @PostMapping("/from-advance")
//    public ResponseEntity<ApiResponse<Payments>> createPaymentFromAdvance(
//            @Valid @RequestBody PaymentRequest request) {
//        Payments payment = paymentService.createPaymentFromAdvance(request);
//        return ResponseEntity.ok(new ApiResponse<>(true,"Payment Success",payment,null));
//    }

    @PostMapping("/advance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payments>> createPaymentFromAdvance(@RequestBody PaymentRequest request) {
        ApiResponse<Payments> response = paymentService.createPaymentFromAdvance(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ApiResponse<List<PaymentHistoryDto>> getPaymentHistory() {
        return paymentService.getPaymentHistory();
    }

    @GetMapping("/history/user/{userId}")
    public ApiResponse<List<PaymentHistoryDto>> getPaymentHistoryByUserId(@PathVariable String userId) {
        return paymentService.getPaymentHistoryByUserId(userId);
    }
}