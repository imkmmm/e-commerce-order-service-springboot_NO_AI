package com.example.orders.order;

public record OrderItemRequest(Long productId, int quantity) {
}
