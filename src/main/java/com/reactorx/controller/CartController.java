package com.reactorx.controller;

import com.reactorx.entity.CartItem;
import com.reactorx.entity.User;
import com.reactorx.repository.UserRepository;
import com.reactorx.service.CartService; // Import the service
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    // Inject Service instead of Repositories
    private final CartService cartService;
    private final UserRepository userRepository;

    // ✅ GET - Get all items in user cart
    // The frontend calls: /api/cart?email=harsh@gmail.com
    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(@RequestParam String email) {
        List<CartItem> cartItems = cartService.getCartItems(email);
        return ResponseEntity.ok(cartItems);
    }

    // ✅ POST - Add product to cart - MATCHES FRONTEND CALL
    // The frontend calls: /api/cart/add?email=...&productId=...&quantity=...
    @PostMapping("/add")
    public ResponseEntity<String> addItemToCart( // Renamed to match client logic
                                                 @RequestParam String email,
                                                 @RequestParam Long productId,
                                                 @RequestParam int quantity) {

        // Pass to service layer for validation and save
        String response = cartService.addToCart(email, productId, quantity);
        return ResponseEntity.ok(response);
    }

    // ✅ DELETE - Remove item from cart - MATCHES FRONTEND CALL
    // The frontend calls: /api/cart/{productId}?email=...
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long productId,
            @RequestParam String email) {

        String response = cartService.removeFromCart(email, productId);
        return ResponseEntity.ok(response);
    }

    // NOTE: The CartRequest DTO is no longer needed in the controller if you use @RequestParam
    // You can delete the inner CartRequest class definition.
}