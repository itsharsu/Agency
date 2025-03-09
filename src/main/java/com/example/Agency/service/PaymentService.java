package com.example.Agency.service;

import com.example.Agency.dto.ApiResponse;
import com.example.Agency.dto.PaymentHistoryDto;
import com.example.Agency.dto.reuests.BatchPaymentRequest;
import com.example.Agency.dto.reuests.PaymentRequest;
import com.example.Agency.model.Payments;
import com.example.Agency.model.User;
import com.example.Agency.repository.PaymentsRepository;
import com.example.Agency.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class PaymentService {

    private final PaymentsRepository paymentRepository;
    private final UserRepository userRepository;

    /**
     * Creates a single payment and updates the user's balance accordingly.
     *
     * @param request the payment request containing user id, amount, etc.
     * @return the saved Payment
     */
    @Transactional
    public Payments createPayment(PaymentRequest request) {
        log.info("Creating payment for userId: {} with amount: {}", request.getUserId(), request.getAmountPaid());

        // Validate that the payment amount is greater than zero
        if (request.getAmountPaid() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        // Retrieve user
        final User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // Update user's balance using the helper method
        updateUserBalanceForPayment(user, request.getAmountPaid());
        userRepository.save(user);
        log.info("Updated balance for userId: {}", user.getUserId());

        // Create and save payment record
        Payments payment = new Payments();
        payment.setUser(user);
        payment.setAmountPaid(request.getAmountPaid());
        payment.setReceivedBy(request.getReceivedBy());
        payment.setPaymentDate(request.getPaymentDate());

        Payments savedPayment = paymentRepository.save(payment);
        log.info("Payment created successfully with id: {}", savedPayment.getPaymentId());
        return savedPayment;
    }

    /**
     * Creates batch payments and updates the users' balances accordingly.
     *
     * @param batchRequest the batch payment request containing multiple payment requests.
     * @return a list of saved Payment records.
     */
    @Transactional
    public List<Payments> createBatchPayments(BatchPaymentRequest batchRequest) {
        log.info("Creating batch payments for {} records", batchRequest.getPayments().size());
        return batchRequest.getPayments().stream().map(request -> {
            // Validate payment amount
            if (request.getAmountPaid() <= 0) {
                throw new IllegalArgumentException(
                        "Invalid amount for user " + request.getUserId() + ": Amount must be positive"
                );
            }

            final User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

            // Update user's balance using the helper method
            updateUserBalanceForPayment(user, request.getAmountPaid());
            userRepository.save(user);
            log.info("Updated balance for userId: {}", user.getUserId());

            // Create payment record
            Payments payment = new Payments();
            payment.setUser(user);
            payment.setAmountPaid(request.getAmountPaid());
            payment.setReceivedBy(request.getReceivedBy());
            payment.setPaymentDate(request.getPaymentDate());

            Payments savedPayment = paymentRepository.save(payment);
            log.info("Payment created for userId: {} with id: {}", user.getUserId(), savedPayment.getPaymentId());
            return savedPayment;
        }).collect(Collectors.toList());
    }

    /**
     * Creates a payment from advance and updates the user's balance accordingly.
     *
     * @param request the payment request for a payment from advance.
     * @return an ApiResponse containing the payment details or error messages.
     */
    @Transactional
    public ApiResponse<Payments> createPaymentFromAdvance(PaymentRequest request) {
        log.info("Creating payment from advance for userId: {} with amount: {}", request.getUserId(), request.getAmountPaid());

        if (request.getAmountPaid() <= 0) {
            return new ApiResponse<>(false, "Invalid payment amount", null, "Payment amount must be greater than zero");
        }

        final Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(false, "User not found", null, "User not found");
        }
        final User user = userOpt.get();

        // Validate that the payment amount does not exceed the due amount
        if (user.getDueAmount() == 0) {
            return new ApiResponse<>(false, "No due amount", null, "The due is 0");
        }
        if (request.getAmountPaid() > user.getDueAmount()) {
            return new ApiResponse<>(false, "Payment amount exceeds due amount", null, "Payment amount exceeds due amount");
        }
        // Validate that sufficient advance is available
        if (user.getAdvance() <= 0 || user.getAdvance() < request.getAmountPaid()) {
            return new ApiResponse<>(false, "Insufficient advance", null, "Insufficient advance or advance is zero");
        }

        // Deduct payment amount from both due amount and advance.
        user.setDueAmount(user.getDueAmount() - request.getAmountPaid());
        user.setAdvance(user.getAdvance() - request.getAmountPaid());
        userRepository.save(user);
        log.info("Deducted payment from user balance for userId: {}", user.getUserId());

        // Create payment record.
        Payments payment = new Payments();
        payment.setUser(user);
        payment.setPaymentDate(request.getPaymentDate());
        payment.setAmountPaid(request.getAmountPaid());
        payment.setReceivedBy(request.getReceivedBy());
        Payments savedPayment = paymentRepository.save(payment);
        log.info("Payment from advance created successfully with id: {}", savedPayment.getPaymentId());
        return new ApiResponse<>(true, "Payment created successfully", savedPayment, null);
    }

    /**
     * Helper method to update a user's balance for a regular payment.
     * <p>
     * If the payment amount exceeds the user's due amount, the excess is added to their advance and the due is set to zero.
     * Otherwise, the due amount is reduced by the payment amount.
     *
     * @param user       the user whose balance is being updated
     * @param amountPaid the payment amount
     */
    private void updateUserBalanceForPayment(final User user, final double amountPaid) {
        final double dueAmount = user.getDueAmount();
        if (amountPaid > dueAmount) {
            double extra = amountPaid - dueAmount;
            user.setAdvance(user.getAdvance() + extra);
            user.setDueAmount(0.0);
        } else {
            user.setDueAmount(dueAmount - amountPaid);
        }
    }



    @Transactional(rollbackOn = Exception.class, value = Transactional.TxType.SUPPORTS)
    public ApiResponse<List<PaymentHistoryDto>> getPaymentHistory() {
        log.info("Fetching payment history");
        List<Payments> payments = paymentRepository.findAll();
        List<PaymentHistoryDto> history = payments.stream().map(payment -> {
            PaymentHistoryDto dto = new PaymentHistoryDto();
            dto.setPaymentId(payment.getPaymentId());
            dto.setPaymentDate(payment.getPaymentDate());
            dto.setAmountPaid(payment.getAmountPaid());
            dto.setReceivedBy(payment.getReceivedBy());
            User user = payment.getUser();
            dto.setUserId(user.getUserId());
            dto.setUserName(user.getUserName());
            dto.setShopName(user.getShopName());
            return dto;
        }).collect(Collectors.toList());
        return new ApiResponse<>(true, "Payment history retrieved successfully", history, null);
    }


    @Transactional(Transactional.TxType.SUPPORTS)
    public ApiResponse<List<PaymentHistoryDto>> getPaymentHistoryByUserId(String userId) {
        log.info("Fetching payment history for userId: {}", userId);
        // Assumes PaymentsRepository has a method: findByUserUserId(String userId)
        List<Payments> payments = paymentRepository.findByUserUserId(userId);
        List<PaymentHistoryDto> history = payments.stream().map(payment -> {
            PaymentHistoryDto dto = new PaymentHistoryDto();
            dto.setPaymentId(payment.getPaymentId());
            dto.setPaymentDate(payment.getPaymentDate());
            dto.setAmountPaid(payment.getAmountPaid());
            dto.setReceivedBy(payment.getReceivedBy());
            User user = payment.getUser();
            dto.setUserId(user.getUserId());
            dto.setUserName(user.getUserName());
            dto.setShopName(user.getShopName());
            return dto;
        }).collect(Collectors.toList());
        return new ApiResponse<>(true, "Payment history retrieved successfully for userId: " + userId, history, null);
    }
}
