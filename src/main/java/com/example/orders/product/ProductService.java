package com.example.orders.product;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idCounter;

    public ProductService(){
        products.put(1L, new Product(1L, "Mechanical Keyboard", new BigDecimal("89.99")));
        products.put(2L, new Product(2L, "Mouse", new BigDecimal("299.99")));
        products.put(3L, new Product(3L, "Cisco 2960 Switch", new BigDecimal("499.99")));
        products.put(4L, new Product(4L, "32 GB SO-DIMM RAM", new BigDecimal("599.99")));

        long maxId = products.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        this.idCounter = new AtomicLong(maxId + 1);


    }

    public List<Product> findAll(){
        return new ArrayList<>(products.values());

    }

    public Optional<Product> findById(Long id){
        return Optional.ofNullable(products.get(id));
    }

    public Product createProduct(CreateProductRequest request){
        long newId =idCounter.getAndIncrement();

        Product product = new Product(newId, request.name(), request.price());
        products.put(newId, product);

        return product;

    }
}
