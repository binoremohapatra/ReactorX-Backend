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
@RequestMapping("/api/checkout") // <-- Keep the base mapping here
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private String getAuthenticatedUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 🌟 FIX: Change the mapping for the 'createOrder' method to avoid conflict 🌟
    // We assume the old POST method in OrderController is redundant and should be here.
    @PostMapping // This maps to POST /api/checkout
    @Transactional
    public ResponseEntity<String> createOrder() {
        String email = getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found after authentication: " + email));

        // --- 1. ORDER PROCESSING (Simulated) ---
        String trackingId = "RX-" + (int)(Math.random() * 9000 + 1000) + "-" + user.getId();

        // --- 2. CRITICAL STEP: Clear the user's cart ---
        cartService.clearCart(user);

        return ResponseEntity.ok("Order successfully placed! Tracking ID: " + trackingId);
    }

    // NOTE: If OrderController#checkout is redundant, consider deleting that entire method/class.
}
