package com.reactorx.repository;

import com.reactorx.entity.Order;
import com.reactorx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    Optional<Order> findByTrackingId(String trackingId);
}
