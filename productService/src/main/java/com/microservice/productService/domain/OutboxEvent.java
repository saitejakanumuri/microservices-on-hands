package com.microservice.productService.domain;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "outbox_event")
@Getter
@Setter
public clas OutboxEvent{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;

    @Lob
    private String payload;
    private boolean published;
    private LocalDateTime createdAt;
}