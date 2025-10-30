package com.reactorx.controller;

import com.reactorx.entity.CartItem;
import com.reactorx.entity.User;
import com.reactorx.repository.CartRepository;
import com.reactorx.repository.ProductRepository;
import com.reactorx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ✅ GET - Get all items in user cart
    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(cartRepository.findByUser(user));
    }

    // ✅ POST - Add product to cart
    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody CartRequest request) {
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if already in cart
        var existing = cartRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartRepository.save(item);
            return ResponseEntity.ok(item);
        }

        CartItem item = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(request.getQuantity())
                .build();
        cartRepository.save(item);
        return ResponseEntity.ok(item);
    }

    // ✅ DELETE - Remove item from cart
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId, @RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        cartRepository.deleteByUserAndProduct(user, product);
        return ResponseEntity.ok("Removed from cart");
    }

    // DTO
    @lombok.Data
    public static class CartRequest {
        private String userEmail;
        private Long productId;
        private int quantity;
    }
}
