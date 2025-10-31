package com.reactorx.controller;

import com.reactorx.dto.OrderSummaryDTO;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService; // Use the service layer

    // Helper to get the authenticated user's email (principal)
    private String getAuthenticatedUserEmail() {
        // This is safe because SecurityConfig ensures this path requires a token
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 🧾 Checkout and create order
    // Endpoint: POST /api/checkout
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkout() {
        try {
            String userEmail = getAuthenticatedUserEmail();
            String trackingId = orderService.placeOrder(userEmail); // Logic moved to Service

            // Return 200 OK with success message and Tracking ID
            return ResponseEntity.ok("✅ Order placed successfully! Tracking ID: " + trackingId);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            // Catches "Cart is empty"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 🧭 Get all orders for the authenticated user
    // Endpoint: GET /api/checkout/orders
    // ❌ FIX: The insecure path variable {userId} has been REMOVED.
    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderSummaryDTO>> getOrdersByUser() {
        String userEmail = getAuthenticatedUserEmail();

        // DTO CONVERSION: Logic in service, returns DTO list
        List<OrderSummaryDTO> orders = orderService.getUserOrders(userEmail);

        // Return 200 OK (will be an empty list if no orders exist)
        return ResponseEntity.ok(orders);
    }
}
