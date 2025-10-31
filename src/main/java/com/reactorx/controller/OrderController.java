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
import org.springframework.transaction.annotation.Transactional; // Import needed

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService; // Use the service layer

    // Helper to get the authenticated user's email (principal)
    private String getAuthenticatedUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 🧾 Checkout and create order
    // 🔒 SECURED: Requires a valid JWT token. UserId is retrieved internally.
    // The previous @RequestParam("userId") Long userId has been removed.
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkout() {
        try {
            String userEmail = getAuthenticatedUserEmail();
            String trackingId = orderService.placeOrder(userEmail); // Logic moved to Service

            // ✅ IMPROVED RESPONSE: Return 200 OK with success message and Tracking ID
            return ResponseEntity.ok("✅ Order placed successfully! Tracking ID: " + trackingId);

        } catch (ResourceNotFoundException e) {
            // User ID not found via email (shouldn't happen if authenticated, but good safeguard)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            // Cart is empty
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 🧭 Get all orders for the authenticated user
    // 🔒 SECURED: Requires a valid JWT token. UserId is retrieved internally.
    // The path variable {userId} has been removed.
    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderSummaryDTO>> getOrdersByUser() {
        String userEmail = getAuthenticatedUserEmail();

        // ✅ DTO CONVERSION: Logic in service, returns DTO list
        List<OrderSummaryDTO> orders = orderService.getUserOrders(userEmail);

        // Return 200 OK, even if the list is empty (no orders yet)
        return ResponseEntity.ok(orders);
    }
}
