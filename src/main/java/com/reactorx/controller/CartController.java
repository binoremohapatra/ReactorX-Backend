package com.reactorx.controller;

import com.reactorx.dto.CartItemDTO;
import com.reactorx.repository.UserRepository;
import com.reactorx.service.CartService; // Inject CartService
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
    // NOTE: For order creation, we need the UserRepository here.
    private final UserRepository userRepository;

    // Helper to get the authenticated user's email (principal)
    private String getAuthenticatedUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // ✅ GET - Get all items in user cart
    // Path: /api/cart
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart() {
        String email = getAuthenticatedUserEmail();
        List<CartItemDTO> cartItems = cartService.getCartItems(email);
        return ResponseEntity.ok(cartItems);
    }

    // ✅ POST - Add product to cart
    // Path: /api/cart/add?productId=...&quantity=...
    @PostMapping("/add")
    public ResponseEntity<String> addItemToCart(
            @RequestParam Long productId,
            @RequestParam int quantity) {

        String email = getAuthenticatedUserEmail();
        String response = cartService.addToCart(email, productId, quantity);
        return ResponseEntity.ok(response);
    }

    // ✅ DELETE - Remove item from cart
    // Path: /api/cart/{productId}
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long productId) {

        String email = getAuthenticatedUserEmail();
        String response = cartService.removeFromCart(email, productId);
        return ResponseEntity.ok(response);
    }
}
