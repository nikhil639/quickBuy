package com.nike.cust.user.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.notification-service.url}")
    private String notificationServiceUrl;

    public void sendWelcomeEmail(Long userId, String email, String firstName) {
        String url = notificationServiceUrl + "/api/notifications/send";
        Map<String, Object> body = Map.of(
                "type", "WELCOME_EMAIL",
                "userId", userId,
                "recipientEmail", email,
                "payload", Map.of("firstName", firstName)
        );
        log.info("Calling notification-service for welcome email: userId={}", userId);
        try {
            restTemplate.postForEntity(url, body, Void.class);
        } catch (Exception e) {
            log.warn("Notification service unavailable: {}", e.getMessage());
        }
    }
}
