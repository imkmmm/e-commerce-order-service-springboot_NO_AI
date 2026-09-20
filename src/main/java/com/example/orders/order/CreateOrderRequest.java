package com.example.orders.order;

import java.util.List;


public record CreateOrderRequest(List<OrderItemRequest> items) {
}
