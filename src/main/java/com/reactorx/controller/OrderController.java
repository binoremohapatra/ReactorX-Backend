package com.reactorx.controller;

import com.reactorx.dto.AddressDTO;
import com.reactorx.dto.CartItemDTO;
import com.reactorx.dto.OrderSummaryDTO;
import com.reactorx.entity.User;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.UserRepository;
import com.reactorx.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final HttpSession httpSession;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderSummaryDTO>> getOrders(Authentication authentication) {
        try {
            List<OrderSummaryDTO> orders = orderService.getOrdersForCurrentUser();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrder(Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

            List<CartItemDTO> userCart = (List<CartItemDTO>) httpSession.getAttribute("userCart");
            AddressDTO shippingAddress = (AddressDTO) httpSession.getAttribute("shippingAddress");

            if (userCart == null || userCart.isEmpty()) {
                logger.warn("Order creation failed: cart is empty for user {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cart is empty.");
            }

            if (shippingAddress == null) {
                logger.warn("Order creation failed: shipping address missing for user {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Shipping address missing.");
            }

            // Create order from cart
            OrderSummaryDTO newOrder = orderService.createOrderFromCart(userCart, shippingAddress, currentUser);

            // Clear session cart + address
            httpSession.removeAttribute("userCart");
            httpSession.removeAttribute("shippingAddress");

            logger.info("Order created successfully for user {}", email);
            return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);

        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error occurred during order creation.");
        }
    }
}
