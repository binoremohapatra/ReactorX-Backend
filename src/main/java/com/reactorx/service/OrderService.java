package com.reactorx.service;

import com.reactorx.dto.OrderItemDTO;
import com.reactorx.dto.OrderSummaryDTO;
import com.reactorx.entity.CartItem;
import com.reactorx.entity.Order;
import com.reactorx.entity.OrderItem;
import com.reactorx.entity.User;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.CartRepository;
import com.reactorx.repository.OrderRepository;
import com.reactorx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    private String generateTrackingId() {
        return "RX-" + LocalDateTime.now().getYear()
                + "-" + (new SecureRandom().nextInt(900000) + 100000); // 6-digit random number
    }

    // 1. PLACE ORDER (Checkout)
    @Transactional
    public String placeOrder(String userEmail) {
        // Retrieve User
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<CartItem> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            // Using a specific RuntimeException for better global handling
            throw new IllegalStateException("Cart is empty! Cannot place an order.");
        }

        // Calculate total using BigDecimal for precision and safety
        BigDecimal total = cartItems.stream()
                .map(item -> {
                    BigDecimal price = item.getProduct().getPrice() != null ? item.getProduct().getPrice() : BigDecimal.ZERO;
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        String trackingId = generateTrackingId();

        // Create Order
        Order order = Order.builder()
                .trackingId(trackingId)
                .status("PROCESSING") // Initial status
                .totalAmount(total)
                .orderDate(LocalDateTime.now())
                .user(user)
                .orderItems(new ArrayList<>())
                .build();

        // Convert CartItems to OrderItems and link them
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getProduct().getPrice())
                    .build();
            order.getOrderItems().add(orderItem);
        }

        orderRepository.save(order);

        // Clear cart
        cartRepository.deleteByUser(user);

        return trackingId; // Return tracking ID for success message
    }

    // 2. GET USER ORDERS
    // Uses DTO mapping
    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Note: findByUser will load Orders, and @Transactional will enable lazy loading
        // of OrderItems later during mapping.
        List<Order> orders = orderRepository.findByUser(user);

        return orders.stream()
                .map(this::mapToOrderSummaryDTO)
                .collect(Collectors.toList());
    }


    // 3. GET ORDER BY TRACKING ID (for non-user route, if implemented)
    @Transactional(readOnly = true)
    public Order trackOrder(String trackingId, String userEmail) {
        Order order = orderRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid tracking ID"));

        if (!order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new SecurityException("Unauthorized to view this order"); // Use SecurityException
        }

        // Force initialization for DTO mapping in Controller or if full entity is needed
        order.getOrderItems().size();

        return order;
    }

    // --- MAPPING HELPERS ---

    private OrderSummaryDTO mapToOrderSummaryDTO(Order order) {
        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setId(order.getTrackingId());
        // Format date nicely
        dto.setDate(order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        // Format total amount
        dto.setTotal(String.format("%.2f", order.getTotalAmount()));
        dto.setStatus(order.getStatus());

        // Map OrderItems to OrderItemDTOs
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::mapToOrderItemDTO)
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);
        return dto;
    }

    private OrderItemDTO mapToOrderItemDTO(OrderItem item) {
        // Assuming Product entity has enough data for name/image
        return new OrderItemDTO(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPriceAtPurchase().setScale(2, RoundingMode.HALF_UP),
                // Placeholder image for simplicity; real logic would parse mediaJson
                "https://placehold.co/50x50/333/fff?text=IMG"
        );
    }
}
