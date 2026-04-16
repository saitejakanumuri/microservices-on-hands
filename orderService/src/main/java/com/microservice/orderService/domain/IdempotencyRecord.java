package com.microservice.orderService.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name="idempotency_record", 
       uniqueConstraints = {
            @UniqueConstraint(columnNames = {"userId", "idempotencyKey"})
       }
)
@Getter
@Setter
public class IdempotencyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String idempotencyKey;
    private String requestPath;
    private String status; //PROCESSING, SUCCESS, FAILED

    @Lob
    private String responseBody;

    private LocalDateTime createdAt;
}