package com.nike.cust.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private static final String TOPIC = "user-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(Long userId, String email, String firstName, String lastName) {
        Map<String, Object> event = Map.of(
                "eventType", "USER_REGISTERED",
                "userId", userId,
                "email", email,
                "firstName", firstName,
                "lastName", lastName,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(TOPIC, String.valueOf(userId), event);
        log.info("Published USER_REGISTERED event for userId={}", userId);
    }
}
