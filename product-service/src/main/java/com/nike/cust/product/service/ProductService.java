package com.nike.cust.product.service;

import com.nike.cust.product.dto.ProductRequest;
import com.nike.cust.product.dto.ProductResponse;
import com.nike.cust.product.kafka.ProductEventProducer;
import com.nike.cust.product.model.Category;
import com.nike.cust.product.model.Product;
import com.nike.cust.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .available(true)
                .build();
        product = productRepository.save(product);
        productEventProducer.publishProductCreated(product.getId(), product.getName(), product.getPrice());
        log.info("Product created: {}", product.getName());
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getByCategory(Category category) {
        return productRepository.findByCategory(category).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProductResponse updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        product.setStockQuantity(quantity);
        product.setAvailable(quantity > 0);
        product = productRepository.save(product);
        productEventProducer.publishInventoryUpdated(product.getId(), product.getName(), quantity);
        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .available(product.isAvailable())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
