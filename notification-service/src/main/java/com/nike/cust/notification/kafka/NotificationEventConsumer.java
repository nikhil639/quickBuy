package com.nike.cust.notification.kafka;

import com.nike.cust.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Asynchronous entry point — consumes events published by all other services.
 * Handles cases where real-time sync notification was not possible
 * (e.g. retries, audit trail, downstream enrichment).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user-events", groupId = "notification-service-group")
    public void onUserEvent(Map<String, Object> event) {
        String type = (String) event.get("eventType");
        Long userId = ((Number) event.get("userId")).longValue();
        log.info("[KAFKA] user-events → eventType={}, userId={}", type, userId);
        notificationService.sendAsync(type, userId, event);
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void onOrderEvent(Map<String, Object> event) {
        String type = (String) event.get("eventType");
        Long userId = ((Number) event.get("userId")).longValue();
        log.info("[KAFKA] order-events → eventType={}, userId={}", type, userId);
        notificationService.sendAsync(type, userId, event);
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void onPaymentEvent(Map<String, Object> event) {
        String type = (String) event.get("eventType");
        Long orderId = ((Number) event.get("orderId")).longValue();
        log.info("[KAFKA] payment-events → eventType={}, orderId={}", type, orderId);
        notificationService.sendAsync(type, null, event);
    }

    @KafkaListener(topics = "product-events", groupId = "notification-service-group")
    public void onProductEvent(Map<String, Object> event) {
        String type = (String) event.get("eventType");
        log.info("[KAFKA] product-events → eventType={}", type);
        notificationService.sendAsync(type, null, event);
    }
}
