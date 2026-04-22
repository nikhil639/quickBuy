package com.nike.cust.product.kafka;

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
public class ProductEventProducer {

    private static final String TOPIC = "product-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishInventoryUpdated(Long productId, String productName, int newStock) {
        Map<String, Object> event = Map.of(
                "eventType", "INVENTORY_UPDATED",
                "productId", productId,
                "productName", productName,
                "newStock", newStock,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(TOPIC, String.valueOf(productId), event);
        log.info("Published INVENTORY_UPDATED event for productId={}, newStock={}", productId, newStock);
    }

    public void publishProductCreated(Long productId, String productName, BigDecimal price) {
        Map<String, Object> event = Map.of(
                "eventType", "PRODUCT_CREATED",
                "productId", productId,
                "productName", productName,
                "price", price,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(TOPIC, String.valueOf(productId), event);
        log.info("Published PRODUCT_CREATED event for productId={}", productId);
    }
}
