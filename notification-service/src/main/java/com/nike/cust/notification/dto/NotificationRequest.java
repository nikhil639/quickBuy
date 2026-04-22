package com.nike.cust.notification.dto;

import com.nike.cust.notification.model.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class NotificationRequest {

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String recipientEmail;

    // Flexible payload: orderId, amount, transactionId, etc.
    private Map<String, Object> payload;
}
