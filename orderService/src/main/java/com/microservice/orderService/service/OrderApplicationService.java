package com.microservice.orderService.service;
import com.microservice.orderService.domain.OrderStatus;
import com.microservice.orderService.domain.event.OrderCreatedEvent;
import com.microservice.orderService.repository.IdempotencyRecordRepository;
import com.microservice.orderService.repository.OutboxEventRepository;
import com.microservice.orderService.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.UUID;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    public CreateOrderResponse createOrder(CreateOrderRequest request, String idempotencyKey) throws Exception {
        Optional<IdempotencyRecord> existingRecord = idempotencyRecordRepository.findByUserIdAndIdempotencyKey(request.getUserId(), idempotencyKey);

        if(existingRecord.isPresent()) {
            IdempotencyRecord record = existingRecord.get();
            if("SUCCESS".equals(record.getStatus())) {
                return objectMapper.readValue(record.getResponseBody(), CreateOrderResponse.class);
            }
            IF("PROCESSING".equals(record.getStatus())) {
                throw new IllegalStateException("Request is still being processed. Please try again later.");
            }
        }

        IdempotencyRecord idempotencyRecord = new IdempotencyRecord();
        idempotencyRecord.setUserId(request.getUserId());
        idempotencyRecord.setIdempotencyKey(idempotencyKey);
        idempotencyRecord.setRequestPath("/orders");
        idempotencyRecord.setStatus("PROCESSING");
        idempotencyRecord.setCreatedAt(LocalDateTime.now());

        try{
            idempotencyRecordRepository.save(idempotencyRecord);
        } catch (DataIntegrityViolationException e) {
            idempotencyRecord alreadySaved = idempotencyRecordRepository.findByUserIdAndIdempotencyKey(request.getUserId(), idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Failed to save idempotency record."));
            if("SUCCESS".equals(alreadySaved.getStatus())) {
                return objectMapper.readValue(alreadySaved.getResponseBody(), CreateOrderResponse.class);
            } 
            throw new IllegalStateException("Request is still being processed. Please try again later.");
            
        }

        String orderRef = UUID.randomUUID().toString();
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setAmount(request.getAmount());
        order.setOrderRef(orderRef);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), order.getUserId(), order.getAmount(), order.getOrderRef());

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("Order");
        outboxEvent.setAggregateId(order.getId());
        outboxEvent.setType("ORDER_CREATED");
        outboxEvent.setPayload(objectMapper.writeValueAsString(event));
        outboxEvent.setCreatedAt(LocalDateTime.now());
        outboxEvent.setIsPublished(false);
        outboxEventRepository.save(outboxEvent);

        CreateOrderResponse response = new CreateOrderResponse(order.getId(), order.getOrderRef(), order.getStatus().name());
        idempotencyRecord.setStatus("SUCCESS");
        idempotencyRecord.setResponseBody(objectMapper.writeValueAsString(response));

        return response;
    }
}