package com.microservice.orderService.repository;

import com.microservice.orderService.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderRef(String orderRef);
}