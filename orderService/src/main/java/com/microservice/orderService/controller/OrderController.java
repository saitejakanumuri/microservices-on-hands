package com.microservice.orderService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.orderService.ProductClient;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private ProductClient productClient;

    @GetMapping("/{orderId}")
    public String getOrders(@PathVariable Long orderId) {
        String product = productClient.getProductById(orderId);
        return "Order: " + orderId + " Product: " + product;
    }
}