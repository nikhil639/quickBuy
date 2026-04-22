package com.nike.cust.order.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.product-service.url}")
    private String productServiceUrl;

    public ProductDetails getProduct(Long productId) {
        String url = productServiceUrl + "/api/products/" + productId;
        log.info("Calling product-service: GET {}", url);
        return restTemplate.getForObject(url, ProductDetails.class);
    }

    @Data
    public static class ProductDetails {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer stockQuantity;
        private boolean available;
    }
}
