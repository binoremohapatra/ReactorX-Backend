package com.reactorx.controller;

import com.reactorx.entity.*;
import com.reactorx.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private String generateTrackingId() {
        return "RX-" + (100000 + new SecureRandom().nextInt(900000));
    }

    // 🧾 Checkout and create order
    @PostMapping
    public ResponseEntity<?> checkout(@RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("❌ Invalid user ID");
        }

        List<CartItem> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body("🛒 Your cart is empty. Add items before checkout.");
        }

        // Calculate total amount
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        String trackingId = generateTrackingId();

        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status("PLACED")
                .totalAmount(totalAmount)
                .trackingId(trackingId)
                .build();

        orderRepository.save(order);

        // Create order items from cart
        for (CartItem item : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(item.getProduct())
                    .quantity(item.getQuantity())
                    .priceAtPurchase(item.getProduct().getPrice())

                    .build();
            order.getOrderItems().add(orderItem);
        }

        orderRepository.save(order);

        // Clear the user's cart
        cartRepository.deleteAll(cartItems);

        return ResponseEntity.ok("✅ Order placed successfully! Tracking ID: " + trackingId);
    }

    // 🧭 Get all orders for a user
    @GetMapping("/orders/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("❌ Invalid user ID");
        }
        List<Order> orders = orderRepository.findByUser(user);
        return ResponseEntity.ok(orders);
    }
}
