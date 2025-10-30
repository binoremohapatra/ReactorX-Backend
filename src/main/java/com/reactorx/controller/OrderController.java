package com.reactorx.controller;

import com.reactorx.entity.Order;
import com.reactorx.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestParam String email) {
        Order order = orderService.placeOrder(email);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestParam String email) {
        return ResponseEntity.ok(orderService.getUserOrders(email));
    }

    @GetMapping("/track")
    public ResponseEntity<?> trackOrder(@RequestParam String trackingId, @RequestParam String email) {
        return ResponseEntity.ok(orderService.trackOrder(trackingId, email));
    }
}
