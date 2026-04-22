package com.nike.cust.payment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentProcessed(Long paymentId, Long orderId, String status) {
        Map<String, Object> event = Map.of(
                "eventType", status,
                "paymentId", paymentId,
                "orderId", orderId,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(TOPIC, String.valueOf(orderId), event);
        log.info("Published {} event for orderId={}", status, orderId);
    }
}
