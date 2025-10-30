package com.reactorx.controller;

import com.reactorx.entity.Order;
import com.reactorx.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://reactorx-frontend.vercel.app",
        "https://reactorx-frontend.onrender.com"
}, allowCredentials = "true")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(@RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(orderService.placeOrder(email));
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> getUserOrders(@RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(orderService.getUserOrders(email));
    }

    @GetMapping("/track")
    public ResponseEntity<Order> trackOrder(@RequestParam String trackingId,
                                            @RequestParam String email) {
        return ResponseEntity.ok(orderService.trackOrder(trackingId, email));
    }
}
