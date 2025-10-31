package com.reactorx.controller;

import com.reactorx.entity.CartItem;
import com.reactorx.service.CartService; // Import the service
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // Ensure CORS is here too
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Helper to get the authenticated user's email (principal)
    private String getAuthenticatedUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // ✅ GET - Get all items in user cart
    // Path: /api/cart (no params needed)
    // 🔒 SECURED via SecurityConfig
    @GetMapping
    public ResponseEntity<List<CartItem>> getCart() {
        String email = getAuthenticatedUserEmail();
        List<CartItem> cartItems = cartService.getCartItems(email);
        return ResponseEntity.ok(cartItems);
    }

    // ✅ POST - Add product to cart - MATCHES FRONTEND CALL
    // Path: /api/cart/add?productId=...&quantity=...
    // Removed @RequestParam String email
    // 🔒 SECURED via SecurityConfig
    @PostMapping("/add")
    public ResponseEntity<String> addItemToCart(
            @RequestParam Long productId,
            @RequestParam int quantity) {

        String email = getAuthenticatedUserEmail();
        // Pass to service layer for validation and save
        String response = cartService.addToCart(email, productId, quantity);
        return ResponseEntity.ok(response);
    }

    // ✅ DELETE - Remove item from cart - MATCHES FRONTEND CALL
    // Path: /api/cart/{productId} (no params needed)
    // Removed @RequestParam String email
    // 🔒 SECURED via SecurityConfig
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long productId) {

        String email = getAuthenticatedUserEmail();
        String response = cartService.removeFromCart(email, productId);
        return ResponseEntity.ok(response);
    }
}
