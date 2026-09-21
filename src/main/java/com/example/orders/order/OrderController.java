package com.example.orders.order;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> findAll(){
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public Order findById(@PathVariable Long id){
        return orderService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@RequestBody @Valid CreateOrderRequest request){
        return orderService.createOrder(request);
    }




}
