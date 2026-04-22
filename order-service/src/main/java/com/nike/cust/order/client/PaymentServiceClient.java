package com.nike.cust.order.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.payment-service.url}")
    private String paymentServiceUrl;

    public PaymentResult processPayment(Long orderId, Long userId, BigDecimal amount) {
        String url = paymentServiceUrl + "/api/payments";
        PaymentRequest request = new PaymentRequest(orderId, userId, amount);
        log.info("Calling payment-service: POST {} for orderId={}", url, orderId);
        ResponseEntity<PaymentResult> response = restTemplate.postForEntity(url, request, PaymentResult.class);
        return response.getBody();
    }

    @Data
    public static class PaymentRequest {
        private final Long orderId;
        private final Long userId;
        private final BigDecimal amount;
    }

    @Data
    public static class PaymentResult {
        private Long id;
        private String transactionId;
        private String status;       // SUCCESS or FAILED
        private String failureReason;
    }
}
