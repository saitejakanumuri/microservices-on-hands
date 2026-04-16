package com.microservice.orderService.service;
import com.microservice.orderService.domain.Order;
import com.microservice.orderService.domain.OrderStatus;
import com.microservice.orderService.domain.event.PaymentCompletedEvent;
import com.microservice.orderService.domain.event.PaymentFailedEvent;
import com.microservice.orderService.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequriredArgsConstructor
public class OrderEventConsumer {

    @KafkaListener(topics = "payment-success-events", groupId = "order-service")
    @Transactional
    public void onPaymentCompleted(String payload) throws Exception {
        PaymentCompletedEvent event = objectMapper.readValue(payload, PaymentCompletedEvent.class);
        Order order  = OrderRepository.findById(event.getOrderId()).orElseThrow();
        order.setStatus(OrderStatus.CONFIRMED);
    }

    @KafkaListener(topics = "payment-failed-events", groupId = "order-service")
    @Transactional
    public void onPaymentFailed(String payload) throws Exception {
        PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);
        Order order = OrderRepository.findById(event.getOrderId()).orElseThrow();
        order.setStatus(OrderStatus.PAYMENT_FAILED);
    }
}