package com.microservice.orderService.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateOrderResponse {
    private Long orderId;
    private String orderRef;
    private String status;
}