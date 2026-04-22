package com.nike.cust.product.dto;

import com.nike.cust.product.model.Category;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Category category;
    private boolean available;
    private LocalDateTime createdAt;
}
