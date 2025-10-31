package com.reactorx.controller;

import com.reactorx.entity.User;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.UserRepository;
import com.reactorx.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class CheckoutController {

    // Inject necessary services
    private final CartService cartService;
    private final UserRepository userRepository;

    private String getAuthenticatedUserEmail() {
        // Retrieves the user's email (which is the principal name)
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Handles the final order submission. Clears the cart upon successful "order placement".
     * Matches the frontend's POST call to /api/checkout.
     * * @return A success message including a simulated tracking ID.
     */
    @PostMapping
    @Transactional // Ensures atomicity of order creation and cart clearing
    public ResponseEntity<String> createOrder() {
        String email = getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found after authentication: " + email));

        // --- 1. ORDER PROCESSING (Simulated) ---
        // In a real app, logic for payment validation, inventory updates,
        // and order saving happens here.
        String trackingId = "RX-" + (int)(Math.random() * 9000 + 1000) + "-" + user.getId();

        // --- 2. CRITICAL STEP: Clear the user's cart ---
        // This is where CartService.clearCart() is called.
        cartService.clearCart(user);

        // --- 3. Return Success Response ---
        return ResponseEntity.ok("Order successfully placed! Tracking ID: " + trackingId);
    }
}
