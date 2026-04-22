package com.nike.cust.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(Long orderId, Long userId, BigDecimal totalAmount) {
        Map<String, Object> event = Map.of(
                "eventType", "ORDER_CREATED",
                "orderId", orderId,
                "userId", userId,
                "totalAmount", totalAmount,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(TOPIC, String.valueOf(orderId), event);
        log.info("Published ORDER_CREATED event for orderId={}", orderId);
    }

    public void publishOrderCancelled(Long orderId, Long userId) {
        Map<String, Object> event = Map.of(
                "eventType", "ORDER_CANCELLED",
                "orderId", orderId,
                "userId", userId,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(TOPIC, String.valueOf(orderId), event);
        log.info("Published ORDER_CANCELLED event for orderId={}", orderId);
    }
}
