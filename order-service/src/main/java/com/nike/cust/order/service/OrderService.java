package com.nike.cust.order.service;

import com.nike.cust.order.client.PaymentServiceClient;
import com.nike.cust.order.client.ProductServiceClient;
import com.nike.cust.order.dto.OrderItemRequest;
import com.nike.cust.order.dto.OrderRequest;
import com.nike.cust.order.dto.OrderResponse;
import com.nike.cust.order.kafka.OrderEventProducer;
import com.nike.cust.order.model.Order;
import com.nike.cust.order.model.OrderItem;
import com.nike.cust.order.model.OrderStatus;
import com.nike.cust.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;   // sync — get product details
    private final PaymentServiceClient paymentServiceClient;   // sync — process payment
    private final OrderEventProducer orderEventProducer;       // async — notification-service listens

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // 1. Resolve each product synchronously from product-service
        List<OrderItem> items = request.getItems().stream()
                .map(this::resolveItem)
                .toList();

        BigDecimal total = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Persist order as PENDING
        Order order = Order.builder()
                .userId(request.getUserId())
                .shippingAddress(request.getShippingAddress())
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .build();
        order.getItems().addAll(items);
        items.forEach(i -> i.setOrder(order));
        Order saved = orderRepository.save(order);
        log.info("Order {} created (PENDING), total={}", saved.getId(), total);

        // 3. Call payment-service synchronously — simulated instant response
        PaymentServiceClient.PaymentResult payment =
                paymentServiceClient.processPayment(saved.getId(), saved.getUserId(), total);

        if ("SUCCESS".equals(payment.getStatus())) {
            saved.setStatus(OrderStatus.CONFIRMED);
            log.info("Order {} confirmed, transactionId={}", saved.getId(), payment.getTransactionId());
        } else {
            saved.setStatus(OrderStatus.PAYMENT_FAILED);
            log.warn("Order {} payment failed: {}", saved.getId(), payment.getFailureReason());
        }
        orderRepository.save(saved);

        // 4. Publish to Kafka — notification-service consumes ORDER_CREATED asynchronously
        //    and handles email/SMS delivery independently
        orderEventProducer.publishOrderCreated(saved.getId(), saved.getUserId(), total);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        // Publish ORDER_CANCELLED — notification-service handles the cancellation email async
        orderEventProducer.publishOrderCancelled(order.getId(), order.getUserId());
        return toResponse(order);
    }

    private OrderItem resolveItem(OrderItemRequest req) {
        ProductServiceClient.ProductDetails product = productServiceClient.getProduct(req.getProductId());
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + req.getProductId());
        }
        if (!product.isAvailable() || product.getStockQuantity() < req.getQuantity()) {
            throw new IllegalStateException("Insufficient stock for product: " + req.getProductId());
        }
        return OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(req.getQuantity())
                .unitPrice(product.getPrice())
                .build();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> OrderResponse.OrderItemResponse.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .toList();
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
