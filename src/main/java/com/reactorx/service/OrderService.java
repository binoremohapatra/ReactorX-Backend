package com.reactorx.service;

import com.reactorx.entity.*;
import com.reactorx.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public Order placeOrder(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty!");
        }

        // Calculate total using BigDecimal for precision
        BigDecimal total = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String trackingId = generateTrackingId();

        Order order = Order.builder()
                .trackingId(trackingId)
                .status("PENDING")
                .totalAmount(total)
                .orderDate(LocalDateTime.now())
                .user(user)
                .items(new ArrayList<>(cartItems))
                .build();

        orderRepository.save(order);

        // Clear cart after successful order
        cartRepository.deleteByUser(user);

        return order;
    }

    public List<Order> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user);
    }

    public Order trackOrder(String trackingId, String userEmail) {
        Order order = orderRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RuntimeException("Invalid tracking ID"));

        if (!order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new RuntimeException("Unauthorized to view this order");
        }

        return order;
    }

    private String generateTrackingId() {
        return "RX-" + LocalDateTime.now().getYear()
                + "-" + (int) (Math.random() * 90000 + 10000);
    }
}
