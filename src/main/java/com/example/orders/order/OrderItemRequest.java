package com.example.orders.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(@NotNull Long productId, @Positive@NotNull int quantity) {
}
