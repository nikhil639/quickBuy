package com.nike.cust.payment.service;

import com.nike.cust.payment.dto.PaymentRequest;
import com.nike.cust.payment.dto.PaymentResponse;
import com.nike.cust.payment.kafka.PaymentEventProducer;
import com.nike.cust.payment.model.Payment;
import com.nike.cust.payment.model.PaymentStatus;
import com.nike.cust.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    /**
     * Synchronous payment processing with simulated response.
     * In production, this would call an external payment gateway (Razorpay, Stripe, etc.)
     * and handle their async callbacks — here we simulate an instant success.
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new IllegalStateException("Payment already processed for order: " + request.getOrderId());
        }

        // Simulated payment — always succeeds (add failure logic as needed for testing)
        PaymentStatus status = simulatePaymentGateway(request);
        String transactionId = status == PaymentStatus.SUCCESS ? UUID.randomUUID().toString() : null;
        String failureReason = status == PaymentStatus.FAILED ? "Simulated gateway decline" : null;

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .status(status)
                .transactionId(transactionId)
                .failureReason(failureReason)
                .processedAt(LocalDateTime.now())
                .build();
        payment = paymentRepository.save(payment);
        log.info("Payment {} for orderId={}, amount={}", status, payment.getOrderId(), payment.getAmount());

        // Publish event to Kafka — notification-service consumes this asynchronously
        String eventType = status == PaymentStatus.SUCCESS ? "PAYMENT_SUCCESS" : "PAYMENT_FAILED";
        paymentEventProducer.publishPaymentProcessed(payment.getId(), payment.getOrderId(), eventType);

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByUser(Long userId) {
        return paymentRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    private PaymentStatus simulatePaymentGateway(PaymentRequest request) {
        // Simulate: orders above 1,000,000 fail (useful for testing failure path)
        if (request.getAmount().longValue() > 1_000_000) {
            return PaymentStatus.FAILED;
        }
        return PaymentStatus.SUCCESS;
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .processedAt(payment.getProcessedAt())
                .build();
    }
}
