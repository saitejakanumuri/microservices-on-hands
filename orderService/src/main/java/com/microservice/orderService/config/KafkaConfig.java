package com.microservice.orderService.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic OrderEventsTopic() {
        return new NewTopic("order-events", 3, (short) 1);
    }

    @Bean
    public NewTopic paymentSuccessTopic() {
        return new NewTopic("payment-success-events", 3, (short) 1);
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return new NewTopic("payment-failed-events", 3, (short) 1);
    }
}