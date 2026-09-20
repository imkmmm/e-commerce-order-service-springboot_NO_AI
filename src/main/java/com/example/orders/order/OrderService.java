package com.example.orders.order;


import com.example.orders.product.Product;
import com.example.orders.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final Map<Long, Order> orders = new HashMap<>();
    private final ProductService productService;

    private final AtomicLong idCounter = new AtomicLong(0);

    public OrderService(ProductService productService){
        this.productService = productService;

    }

    public List<Order> findAll(){
        return new ArrayList<>(orders.values());

    }

    public Optional<Order> findById(Long id){
        return Optional.ofNullable(orders.get(id));
    }


    public Order createOrder(CreateOrderRequest request){

        long orderId = idCounter.incrementAndGet();
        List<OrderItem> items = new ArrayList<>();

        for(OrderItemRequest itemRequest : request.items()){

            Product product = productService.findById(itemRequest.productId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + itemRequest.productId()));
            items.add(new OrderItem(product, itemRequest.quantity()));

        }

        Order order = new Order(orderId, items);
        orders.put(orderId, order);

        return order;
    }

}
