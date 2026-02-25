package com.microservice.orderService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Component;

@FeignClient(name = "productService")
public interface ProductClient {
    @GetMapping("/products/{productId}")
    String getProductById(@PathVariable Long productId);

}
