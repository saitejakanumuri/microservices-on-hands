package com.microservice.paymentService.service;

import com.microservice.paymentService.domain.OutboxEvent;
import com.microservice.paymentService.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentOutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 3000)
    public void publish() {
        List<OutboxEvent> events = outboxEventRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            if ("PAYMENT_COMPLETED".equals(event.getEventType())) {
                kafkaTemplate.send("payment-success-events", event.getAggregateId(), event.getPayload());
            } else if ("PAYMENT_FAILED".equals(event.getEventType())) {
                kafkaTemplate.send("payment-failed-events", event.getAggregateId(), event.getPayload());
            }

            event.setPublished(true);
            outboxEventRepository.save(event);
        }
    }
}