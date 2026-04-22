package com.nike.cust.notification.dto;

import com.nike.cust.notification.model.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String notificationId;
    private NotificationType type;
    private Long userId;
    private String status;
    private LocalDateTime sentAt;
}
