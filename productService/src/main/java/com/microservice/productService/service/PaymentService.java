package com.microservice.paymentService.service;

import com.microservice.paymentService.domain.*;
import com.microservice.paymentService.domain.event.PaymentCompletedEvent;
import com.microservice.paymentService.domain.event.PaymentFailedEvent;
import com.microservice.paymentService.repository.AccountRepository;
import com.microservice.paymentService.repository.OutboxEventRepository;
import com.microservice.paymentService.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void debit(Long orderId, Long userId, java.math.BigDecimal amount) throws Exception {
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            return; // idempotent consumer behavior
        }

        Account account = accountRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(orderId, "Insufficient balance");
            saveOutbox("PAYMENT", String.valueOf(orderId), "PAYMENT_FAILED",
                    objectMapper.writeValueAsString(failedEvent));
            return;
        }

        account.setBalance(account.getBalance().subtract(amount));

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setPaymentRef(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.DEBITED);
        payment.setCreatedAt(LocalDateTime.now());

        try {
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            return; // duplicate delivery or parallel handling
        }

        PaymentCompletedEvent completedEvent =
                new PaymentCompletedEvent(orderId, payment.getPaymentRef());

        saveOutbox("PAYMENT", String.valueOf(payment.getId()), "PAYMENT_COMPLETED",
                objectMapper.writeValueAsString(completedEvent));
    }

    @Transactional
    public void refund(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return;
        }

        Account account = accountRepository.findByUserIdForUpdate(payment.getUserId()).orElseThrow();
        account.setBalance(account.getBalance().add(payment.getAmount()));
        payment.setStatus(PaymentStatus.REFUNDED);
    }

    private void saveOutbox(String aggregateType, String aggregateId, String eventType, String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType(aggregateType);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setEventType(eventType);
        outboxEvent.setPayload(payload);
        outboxEvent.setPublished(false);
        outboxEvent.setCreatedAt(LocalDateTime.now());
        outboxEventRepository.save(outboxEvent);
    }
}