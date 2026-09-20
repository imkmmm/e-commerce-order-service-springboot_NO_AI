package com.example.orders.product;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class ProductService {

    private final Map<Long, Product> products = new HashMap<>();

    public ProductService(){
        products.put(1L, new Product(1L, "Mechanical Keyboard", new BigDecimal("89.99")));
        products.put(2L, new Product(2L, "Mouse", new BigDecimal("299.99")));
        products.put(3L, new Product(3L, "Cisco 2960 Switch", new BigDecimal("499.99")));
        products.put(4L, new Product(4L, "32 GB SO-DIMM RAM", new BigDecimal("599.99")));

    }

    public List<Product> findAll(){
        return new ArrayList<>(products.values());

    }

    public Optional<Product> findById(Long id){
        return Optional.ofNullable(products.get(id));
    }
}
