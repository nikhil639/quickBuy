package com.nike.cust.notification.service;

import com.nike.cust.notification.dto.NotificationRequest;
import com.nike.cust.notification.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    public NotificationResponse send(NotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();

        // Simulated delivery — in production this would call an email/SMS provider
        switch (request.getType()) {
            case WELCOME_EMAIL ->
                log.info("[EMAIL] Welcome notification → userId={}, email={}",
                        request.getUserId(), request.getRecipientEmail());
            case ORDER_CONFIRMATION ->
                log.info("[EMAIL] Order confirmation → userId={}, payload={}",
                        request.getUserId(), request.getPayload());
            case PAYMENT_SUCCESS ->
                log.info("[EMAIL] Payment success receipt → userId={}, payload={}",
                        request.getUserId(), request.getPayload());
            case PAYMENT_FAILED ->
                log.info("[EMAIL] Payment failed alert → userId={}, payload={}",
                        request.getUserId(), request.getPayload());
            case ORDER_CANCELLED ->
                log.info("[EMAIL] Order cancelled → userId={}, payload={}",
                        request.getUserId(), request.getPayload());
            case ORDER_SHIPPED ->
                log.info("[SMS] Shipment update → userId={}, payload={}",
                        request.getUserId(), request.getPayload());
        }

        return NotificationResponse.builder()
                .notificationId(notificationId)
                .type(request.getType())
                .userId(request.getUserId())
                .status("SENT")
                .sentAt(LocalDateTime.now())
                .build();
    }

    // Called by Kafka consumer — same logic, no response needed
    public void sendAsync(String type, Long userId, Object details) {
        log.info("[ASYNC][{}] Notification for userId={} → {}", type, userId, details);
    }
}
