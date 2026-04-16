package com.microservice.orderService.service;
import com.microservice.orderService.domain.OutboxEvent;
import com.microservice.orderService.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 3000) // Every 3 seconds
    public void publish() {
        List<OutboxEvent> events = outboxEventRepository.findTop100ByIsPublishedFalseOrderByCreatedAtAsc();

        for(OutboxEvent event : events){
            kafkaTemplate.send("order-events", event.getAggregateId().toString(), event.getPayload());
            event.setIsPublished(true);
            outboxEventRepository.save(event);
        }
    } 
}