package com.example.orders.order;

import java.math.BigDecimal;
import java.util.List;

public class Order {
    private final Long id;
    private final List<OrderItem> items;


    public Order(Long id, List<OrderItem> items){
        this.id = id;
        this.items = items;
    }

    public  Long getId(){
        return id;
    }

    public List<OrderItem> getItems(){
        return items;
    }

    public BigDecimal getTotal(){

        BigDecimal total = BigDecimal.ZERO;

        for(OrderItem item : items){
            BigDecimal price = item.getProduct().getPrice();
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            BigDecimal lineTotal = price.multiply(quantity);

            total = total.add(lineTotal);

        }

        return total;
    }
}
