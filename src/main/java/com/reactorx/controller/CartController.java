package com.reactorx.controller;

import com.reactorx.entity.CartItem;
import com.reactorx.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://reactorx-frontend.vercel.app",
        "https://reactorx-frontend.onrender.com"
}, allowCredentials = "true")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(@RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(cartService.getCartItems(email));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestHeader("X-User-Email") String email,
                                            @RequestParam Long productId,
                                            @RequestParam(defaultValue = "1") int quantity) {
        return ResponseEntity.ok(cartService.addToCart(email, productId, quantity));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFromCart(@RequestHeader("X-User-Email") String email,
                                                 @RequestParam Long productId) {
        return ResponseEntity.ok(cartService.removeFromCart(email, productId));
    }
}
