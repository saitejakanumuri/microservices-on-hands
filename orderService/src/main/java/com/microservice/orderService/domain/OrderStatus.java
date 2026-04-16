package com.microservice.orderService.domain;
public enum OrderStatus {
    PENDING_PAYMENT,
    PAYMENT_FAILED,
    CONFIRMED,
    CANCELLED
}